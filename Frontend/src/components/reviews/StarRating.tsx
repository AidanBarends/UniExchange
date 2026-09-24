/*
  Stars, for both reading and writing a rating.

  Interactive mode is a real radio group rather than clickable divs, so it works
  with a keyboard and announces itself to a screen reader. Display mode is a
  single labelled image, because eleven separate "star" announcements is noise.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

const VALUES = [1, 2, 3, 4, 5] as const

type StarRatingProps = {
  value: number
  onChange?: (value: number) => void
  className?: string
}

function Star({ filled, className = 'size-5' }: { filled: boolean; className?: string }) {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill={filled ? 'currentColor' : 'none'}
      stroke="currentColor"
      strokeWidth="2"
      aria-hidden="true"
    >
      <path
        d="M12 3l2.7 5.5 6.1.9-4.4 4.3 1 6-5.4-2.9-5.4 2.9 1-6L3.2 9.4l6.1-.9L12 3z"
        strokeLinejoin="round"
      />
    </svg>
  )
}

export function StarRating({ value, onChange, className = '' }: StarRatingProps) {
  if (!onChange) {
    return (
      <span
        className={`inline-flex items-center text-amber-500 ${className}`}
        role="img"
        aria-label={`${value} out of 5`}
      >
        {VALUES.map((star) => (
          <Star key={star} filled={star <= Math.round(value)} />
        ))}
      </span>
    )
  }

  return (
    <span role="radiogroup" aria-label="Rating" className={`inline-flex items-center gap-1 ${className}`}>
      {VALUES.map((star) => (
        <button
          key={star}
          type="button"
          role="radio"
          aria-checked={value === star}
          aria-label={`${star} star${star === 1 ? '' : 's'}`}
          onClick={() => onChange(star)}
          className={`rounded p-0.5 transition hover:scale-110 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-600 ${
            star <= value ? 'text-amber-500' : 'text-gray-300'
          }`}
        >
          <Star filled={star <= value} className="size-7" />
        </button>
      ))}
    </span>
  )
}
