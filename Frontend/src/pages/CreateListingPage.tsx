/*
  Post something for sale.

  OWNER: Mogamat Wazeer Gilbert (221374698)
  ROUTE: /listings/new

  TODO
   - listingsApi.categories()                  GET /api/categories
   - listingsApi.create({ sellerId, categoryId, campusId, title, description,
                          price, status: 'ACTIVE' })
                                               POST /api/listings
   - sellerId and campusId both come from useAuth().user - do not ask the user
   - listingsApi.addImage({ listingId, imageUrl, position, isPrimary })
     POST /api/listing-images. There is NO file upload on the backend: it stores
     a URL string, so take a URL for now
   - on success: navigate(`/listings/${created.listingId}`)
   - build the form with react-hook-form + zod, exactly like SignUpPage does.
     Put the schema in src/lib/schemas.ts next to signUpSchema
   - the backend factory rejects a blank title and a negative price with 400 and
     a `fields` map - surface those the way SignUpPage does
   - reuse: PageHeader, TextField, Textarea, Select, Button, Alert

*/

import React, {useState, useEffect} from 'react'
import {useNavigate} from 'react-router-dom'
import {useForm} from 'react-hook-form'
import {zodResolver} from "@hookform/resolvers/zod";
import {useAuth} from '@/auth/useAuth';
import {listingsApi} from "@/lib/api/listings";
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

export const CreateListingPage: React.FC = () => {
    const navigate = useNavigate();
    const {user} = useAuth();

    const [categories, setCategories] = useState<CategoryOption[]>([]);
    const [isLoadingCategories, setIsLoadingCategories] = useState(true);
    const [rootError, setRootError] = useState<string | null>(null);

    const {
        register,
        handleSubmit,
        setError,
        formState: {errors, isSubmitting},
    } = useForm({
        resolver: zodResolver(createListingSchema),
        defaultValues: {
            title: '',
            categoryId: '',
            price: '',
            description: '',
            imageUrl: '',
        },
    });

    useEffect(() => {
        let mounted = true;

        async function fetchCategories() {
            try {
                setIsLoadingCategories(true);
                const data = await listingsApi.categories()
                if (mounted) {
                    setCategories(data || []);
                }
            } catch (err: unknown) {
                if (mounted) {
                    const msg = String((err as { message?: unknown })?.message ?? 'Failed to load marketplace categories.');
                    setRootError(msg);
                }
            } finally {
                if (mounted) {
                    setIsLoadingCategories(false);
                }
            }
        }

        fetchCategories();

        return () => {
            mounted = false;
        };
    }, []);

    const onSubmit = async (data: CreateListingFormData) => {
        setRootError(null)

        if (!user?.userId || !user?.campusId) {
            setRootError('Your account must have a verified campus to create a listing!')
            return;
        }

        try {
            const created = await listingsApi.create({
                sellerId: Number(user.userId),
                campusId: Number(user.campusId),
                categoryId: Number(data.categoryId),
                title: data.title.trim(),
                description: data.description?.trim() || '',
                price: Number(data.price),
                status: 'ACTIVE'
            });

            if (data.imageUrl && data.imageUrl.trim() && created?.listingId) {
                try {
                    await listingsApi.addImage({
                        listingId: Number(created.listingId),
                        imageUrl: data.imageUrl.trim(),
                        position: 1,
                        isPrimary: true,
                    });
                } catch (imgError) {
                    console.warn('Failed to add image to listing', imgError);
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

            setRootError(
                String((apiResponse as Record<string, unknown>)['message'] ?? (err as { message?: unknown })?.message ?? 'Failed to add listing')
            );
        }
    };

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

                    <TextField label="Image URL"
                               placeholder="https://example.com/item.jpg"
                               error={errors.imageUrl?.message}
                               {...register('imageUrl')}
                    />

                    <div className="flex items-center justify-end gap-3 pt-4 border-t border-border">
                        <Button type="button"
                                variant="ghost"
                                onClick={() => navigate(-1)}
                                disabled={isSubmitting}
                        >Cancel</Button>
                        <Button type="submit"
                                disabled={isSubmitting || isLoadingCategories}>
                            {isSubmitting ? 'Posting Listing...' : 'Post Listing'}
                        </Button>
                    </div>
                </form>
            </Card>
        </div>
    );
};

export default CreateListingPage;
