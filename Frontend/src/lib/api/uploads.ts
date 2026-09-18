/*
  File uploads. Backed by POST /api/uploads on UploadController, which saves
  the file to disk and returns the URL it is now reachable at - the same kind
  of URL string listingsApi.addImage and bulletinApi.addImage already expect,
  so callers just plug the result straight into those.
*/

import { authedUpload } from './client'

export const uploadsApi = {
  image: (file: File) => authedUpload<{ url: string }>('/api/uploads', file),
}
