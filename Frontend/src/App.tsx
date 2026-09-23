/*
  Every route in the app.

  Structure, outside in:
    <ProtectedRoute>   redirects to /login when there is no session
      <AppLayout>      top bar, nav and mobile tab bar - pages render inside it
        the page

  So a new signed-in page is ONE line inside the AppLayout block, and it gets the
  chrome for free. Public routes (auth screens, 404) sit outside both.

  Keep the "*" catch-all last.

  Author: Mogamat Yaseen Kannemeyer 240453182
*/

import { Navigate, Route, Routes } from 'react-router-dom'

import { ProtectedRoute } from '@/auth/ProtectedRoute'
import { useAuth } from '@/auth/useAuth'
import { AppLayout } from '@/components/layout/AppLayout'
import { BulletinPage } from '@/pages/BulletinPage'
import { ChatPage } from '@/pages/ChatPage'
import { CreateListingPage } from '@/pages/CreateListingPage'
import { FeedPage } from '@/pages/FeedPage'
import { ListingDetailsPage } from '@/pages/ListingDetailsPage'
import { LoginPage } from '@/pages/LoginPage'
import { MessagesPage } from '@/pages/MessagesPage'
import { NotFoundPage } from '@/pages/NotFoundPage'
import { NotificationsPage } from '@/pages/NotificationsPage'
import { ProfilePage } from '@/pages/ProfilePage'
import { SignUpPage } from '@/pages/SignUpPage'
import { VerifyOtpPage } from '@/pages/VerifyOtpPage'

export default function App() {
  const { isAuthenticated } = useAuth()

  return (
    <Routes>
      <Route index element={<Navigate to={isAuthenticated ? '/feed' : '/login'} replace />} />

      {/* Public - auth screens use AuthLayout, not AppLayout. */}
      <Route path="/signup" element={<SignUpPage />} />
      <Route path="/verify" element={<VerifyOtpPage />} />
      <Route path="/login" element={<LoginPage />} />

      {/* Signed in. Everything here gets the app shell automatically. */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          {/* Joshua Reid Adams (230317693) */}
          <Route path="/feed" element={<FeedPage />} />

          {/* Mogamat Wazeer Gilbert (221374698) - static path, so it wins over
              /listings/:listingId regardless of the order here. */}
          <Route path="/listings/new" element={<CreateListingPage />} />

          {/* Aidan Barends (230255639) */}
          <Route path="/listings/:listingId" element={<ListingDetailsPage />} />

          {/* Raul Ja'aim Everts (230270565) - one component, both routes. */}
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/profile/:userId" element={<ProfilePage />} />

          {/* unassigned */}
          <Route path="/notifications" element={<NotificationsPage />} />
          <Route path="/messages" element={<MessagesPage />} />
          <Route path="/messages/:conversationId" element={<ChatPage />} />
          <Route path="/bulletin" element={<BulletinPage />} />
        </Route>
      </Route>

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}
