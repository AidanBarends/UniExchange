/*

  OWNER: Mogamat Wazeer Gilbert (221374698)
  ROUTE: /listings/new

*/

import React, {useState, useEffect} from 'react'
import {useNavigate} from 'react-router-dom'
import {useForm} from 'react-hook-form'
import {zodResolver} from "@hookform/resolvers/zod";
import {useAuth} from '@/auth/useAuth';
import {authApi} from "@/lib/api/auth";
import {listingsApi} from "@/lib/api/listings";
import type {Campus} from "@/lib/api/types";
import {createListingSchema} from '@/lib/schemas';
import type { CreateListingFormData } from '@/lib/schemas';
import { PageHeader } from '@/components/layout/PageHeader'
import {Card} from  '@/components/ui/Card';
import {TextField} from '@/components/ui/TextField';
import {Textarea} from "@/components/ui/Textarea";
import {Select} from "@/components/ui/Select";
import {Button} from "@/components/ui/Button";
import {Alert} from "@/components/ui/Alert";

interface CategoryOption {
    categoryId: number;
    name: string;
}

const DEFAULT_CATEGORIES = [
    'Electronics',
    'Textbooks',
    'Clothes',
    'Furniture',
    'Stationery',
    'Sports & Fitness',
    'Household Items',
    'Other',
] as const;

const CAMPUS_NAMES = [
    'Bellville Campus',
    'Granger Bay Campus',
    'Mowbray Campus',
    'Wellington Campus',
    'District Six campus',
] as const;

const DEFAULT_CAMPUSES: Campus[] = CAMPUS_NAMES.map((name, index) => ({
    campusId: index + 1,
    name,
    city: '',
    address: null,
}));

export const CreateListingPage: React.FC = () => {
    const navigate = useNavigate();
    const {user} = useAuth();

    const [categories, setCategories] = useState<CategoryOption[]>([]);
    const [campuses, setCampuses] = useState<Campus[]>([]);
    const [isLoadingCategories, setIsLoadingCategories] = useState(true);
    const [isLoadingCampuses, setIsLoadingCampuses] = useState(true);
    const [imageFile, setImageFile] = useState<File | null>(null);
    const [rootError, setRootError] = useState<string | null>(null);

    const {
        register,
        handleSubmit,
        setError,
        formState: {errors, isSubmitting},
    } = useForm<CreateListingFormData>({
        resolver: zodResolver(createListingSchema),
        defaultValues: {
            title: '',
            categoryId: '',
            campusId: '',
            price: '',
            description: '',
        },
    });

    useEffect(() => {
        let mounted = true;

        async function fetchFormOptions() {
            try {
                setIsLoadingCategories(true);
                setIsLoadingCampuses(true);
                const [categoryData, campusData] = await Promise.all([
                    listingsApi.categories(),
                    authApi.campuses(),
                ]);
                if (mounted) {
                    const availableCategories = categoryData || [];
                    const categoriesByName = new Map(
                        availableCategories.map((category) => [category.name.trim().toLowerCase(), category]),
                    );
                    const defaultCategories = DEFAULT_CATEGORIES.map((name, index) =>
                        categoriesByName.get(name.toLowerCase()) ?? {
                            categoryId: index + 1,
                            name,
                        },
                    );
                    const additionalCategories = availableCategories.filter(
                        (category) => !DEFAULT_CATEGORIES.some(
                            (name) => name.toLowerCase() === category.name.trim().toLowerCase(),
                        ),
                    );
                    setCategories([...defaultCategories, ...additionalCategories]);
                    const availableCampuses = campusData || [];
                    const campusesByName = new Map(
                        availableCampuses.flatMap((campus) => [
                            [campus.name.trim().toLowerCase(), campus],
                            [`${campus.name.trim().toLowerCase()} campus`, campus],
                        ]),
                    );
                    const defaultCampuses = DEFAULT_CAMPUSES.map((campus) =>
                        campusesByName.get(campus.name.toLowerCase()) ??
                        campusesByName.get(campus.name.replace(/ campus$/i, '').toLowerCase()) ??
                        campus,
                    );
                    const additionalCampuses = availableCampuses.filter(
                        (campus) => !defaultCampuses.some(
                            (defaultCampus) => defaultCampus.campusId === campus.campusId,
                        ),
                    );
                    setCampuses([...defaultCampuses, ...additionalCampuses]);
                }
            } catch (err: unknown) {
                if (mounted) {
                    const msg = String((err as { message?: unknown })?.message ?? 'Failed to load marketplace categories.');
                    setRootError(msg);
                }
            } finally {
                if (mounted) {
                    setIsLoadingCategories(false);
                    setIsLoadingCampuses(false);
                }
            }
        }

        fetchFormOptions();

        return () => {
            mounted = false;
        };
    }, []);

    const onSubmit = async (data: CreateListingFormData) => {
        setRootError(null)

        if (!user?.userId) {
            setRootError('You must be signed in to create a listing.')
            return;
        }

        if (!imageFile) {
            setRootError('Please choose an image from your device.')
            return;
        }

        if (imageFile) {
            if (!imageFile.type.startsWith('image/')) {
                setRootError('Please select an image file.')
                return;
            }
            if (imageFile.size > 10 * 1024 * 1024) {
                setRootError('Image must be 10 MB or smaller.')
                return;
            }
        }

        try {
            const created = await listingsApi.create({
                sellerId: Number(user.userId),
                campusId: Number(data.campusId),
                categoryId: Number(data.categoryId),
                title: data.title.trim(),
                description: data.description?.trim() || '',
                price: Number(data.price),
                status: 'ACTIVE'
            });

            if (created?.listingId && imageFile) {
                try {
                    await listingsApi.uploadImage({
                        listingId: Number(created.listingId),
                        file: imageFile,
                        position: 1,
                        isPrimary: true,
                    });
                } catch (imgError) {
                    const message = String(
                        (imgError as { message?: unknown })?.message ??
                        'The listing was created, but the image could not be saved.',
                    );
                    setRootError(`Listing created, but image upload failed: ${message}`);
                    return;
                }
            }

            navigate(`/listings/${created.listingId}`);
        } catch (err: unknown) {
            const apiResponse = (err as { response?: { data?: unknown } })?.response?.data;

            if (apiResponse && typeof apiResponse === 'object') {
                const fields = (apiResponse as Record<string, unknown>)['fields'];
                if (fields && typeof fields === 'object') {
                    Object.entries(fields as Record<string, unknown>).forEach(([fieldName, message]) => {
                        setError(fieldName as keyof CreateListingFormData, {
                            type: 'server',
                            message: String(message),
                        });
                    });
                }
            }

            const apiMessage =
                apiResponse && typeof apiResponse === 'object'
                    ? (apiResponse as Record<string, unknown>)['message']
                    : undefined;
            setRootError(
                String(apiMessage ?? (err as { message?: unknown })?.message ?? 'Failed to add listing')
            );
        }
    };

    const campusOptions = campuses
        .filter((campus) => CAMPUS_NAMES.includes(campus.name as typeof CAMPUS_NAMES[number]) ||
            DEFAULT_CAMPUSES.some((defaultCampus) => defaultCampus.campusId === campus.campusId))
        .map((campus) => ({
            ...campus,
            displayName: CAMPUS_NAMES.find((name) =>
                name.toLowerCase() === campus.name.trim().toLowerCase() ||
                name.replace(/ campus$/i, '').toLowerCase() === campus.name.trim().toLowerCase(),
            ) ?? campus.name,
        }));

    return (
        <div className="max-w-2xl mx-auto px-4 py-8 space-y-6">
            <PageHeader
                title="Create a Listing"
                subtitle="Sell textbooks, stationary, gear, or res items directly to peers on your campus."
            />

            {rootError && <Alert tone="error">{rootError}</Alert>}

            <Card className="p-6 md:p-8">
                <form onSubmit={handleSubmit(onSubmit)} className="space-y-6" noValidate>
                    <TextField
                        label="Title"
                        placeholder="e.g. Contemporary Project Management:Plan-Driven and Agile Approaches, Fifth Edition"
                        error={errors.title?.message}
                        {...register('title')}
                    />

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <Select
                            label="Category"
                            error={errors.categoryId?.message}
                            disabled={isLoadingCategories}
                            {...register('categoryId')}>
                            <option value="">
                                {isLoadingCategories ? 'Loading Categories...' : 'Select a category'}
                            </option>
                            {categories.map((cat) => (
                                <option key={cat.categoryId} value={String(cat.categoryId)}>{cat.name}</option>
                            ))}
                        </Select>

                        <Select
                            label="Campus"
                            error={errors.campusId?.message}
                            disabled={isLoadingCampuses}
                            {...register('campusId')}>
                            <option value="">
                                {isLoadingCampuses ? 'Loading campuses...' : 'Select a campus'}
                            </option>
                            {campusOptions.map((campus) => (
                                <option key={campus.campusId} value={String(campus.campusId)}>
                                    {campus.displayName}
                                </option>
                            ))}
                        </Select>

                        <TextField label="Price (ZAR)"
                                   type="number"
                                   step="0.01"
                                   placeholder="0.00"
                                   error={errors.price?.message}
                                   {...register('price')}
                        />
                    </div>

                    <Textarea label="Description"
                              placeholder="Describe condition and extra details... "
                              rows={4}
                              error={errors.description?.message}
                              {...register('description')}
                    />

                    <div className="space-y-1.5">
                        <label htmlFor="imageFile" className="block text-sm font-medium text-ink-700">
                            Upload image
                        </label>
                        <input
                            id="imageFile"
                            type="file"
                            accept="image/jpeg,image/png,image/gif,image/webp"
                            onChange={(event) => setImageFile(event.target.files?.[0] ?? null)}
                            className="sr-only"
                        />
                        <label
                            htmlFor="imageFile"
                            className="inline-flex cursor-pointer items-center gap-2 rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm font-medium text-ink-700 hover:bg-gray-50"
                        >
                            <svg
                                aria-hidden="true"
                                className="h-4 w-4"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="2"
                                strokeLinecap="round"
                                strokeLinejoin="round"
                            >
                                <path d="M3 6a2 2 0 0 1 2-2h5l2 2h7a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6Z" />
                                <path d="M3 8h18" />
                            </svg>
                            Choose file
                        </label>
                        {imageFile && (
                            <span className="ml-3 text-sm text-ink-600">{imageFile.name}</span>
                        )}
                        <p className="text-xs text-ink-400">
                            Choose a JPEG, PNG, GIF, or WebP image up to 10 MB. An image from your device is required.
                        </p>
                    </div>

                    <div className="flex items-center justify-end gap-3 pt-4 border-t border-border">
                        <Button type="button"
                                variant="ghost"
                                onClick={() => navigate(-1)}
                                disabled={isSubmitting}
                        >Cancel</Button>
                        <Button type="submit"
                                disabled={isSubmitting || isLoadingCategories || isLoadingCampuses}>
                            {isSubmitting ? 'Posting Listing...' : 'Post Listing'}
                        </Button>
                    </div>
                </form>
            </Card>
        </div>
    );
};

export default CreateListingPage;
