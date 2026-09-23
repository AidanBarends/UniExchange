/*
  Form validation. zod v4, so `z.email()` rather than the removed
  `z.string().email()`.

  The student-email rule mirrors the backend's app.auth.student-email-pattern.
  Both default to 8-10 digits, deliberately loose on length: CPUT publishes no
  student-number digit count anywhere, and a gate one digit too strict silently
  locks real students out. The domain is what is enforced strictly - and the
  emailed code is what actually proves the mailbox exists.
*/

import { z } from 'zod'

const DEFAULT_STUDENT_EMAIL_PATTERN = String.raw`^\d{8,10}@mycput\.ac\.za$`

function studentEmailPattern(): RegExp {
  const configured = import.meta.env.VITE_STUDENT_EMAIL_PATTERN
  try {
    return new RegExp(configured?.trim() || DEFAULT_STUDENT_EMAIL_PATTERN, 'i')
  } catch {
    // A typo in the env var must never open the gate to every address.
    return new RegExp(DEFAULT_STUDENT_EMAIL_PATTERN, 'i')
  }
}

const STUDENT_EMAIL = studentEmailPattern()

export const studentEmailSchema = z
  .string()
  .trim()
  .min(1, 'Enter your student email')
  .toLowerCase()
  .regex(STUDENT_EMAIL, 'Use your CPUT student email, for example 240453182@mycput.ac.za')

// Matches the backend's @Size(min = 8) on RegisterRequest.password.
const passwordSchema = z.string().min(8, 'Use at least 8 characters')

export const signUpSchema = z
  .object({
    firstName: z.string().trim().min(1, 'Enter your first name'),
    lastName: z.string().trim().min(1, 'Enter your last name'),
    email: studentEmailSchema,
    campusId: z.string().optional(),
    password: passwordSchema,
    confirmPassword: z.string(),
  })
  .refine((values) => values.password === values.confirmPassword, {
    message: 'Passwords do not match',
    path: ['confirmPassword'],
  })

export const loginSchema = z.object({
  // Login accepts the address as typed; the backend decides if it is known.
  // Being strict here would block a future faculty or vendor account.
  email: z.string().trim().min(1, 'Enter your email').toLowerCase(),
  password: z.string().min(1, 'Enter your password'),
  /*
    "Remember me on this device". Plain z.boolean() with a useForm defaultValue
    rather than .default(false): in zod v4 a .default() makes the schema's input
    and output types differ, and zodResolver then infers a LoginValues where the
    field is optional - which quietly turns `values.rememberMe` into
    `boolean | undefined` at every call site.
  */
  rememberMe: z.boolean(),
})

export const otpSchema = z.object({
  code: z
    .string()
    .trim()
    .regex(/^\d{6}$/, 'Enter the 6-digit code'),
})

export type SignUpValues = z.infer<typeof signUpSchema>
export type LoginValues = z.infer<typeof loginSchema>
export type OtpValues = z.infer<typeof otpSchema>
