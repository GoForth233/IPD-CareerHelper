import request from '@/utils/request';

/**
 * Upload a file to OSS via the backend.
 *
 * The backend returns a bare object key (e.g. `resumes/uuid.pdf`), NOT a
 * publicly loadable URL. Persist the key with the entity (resume row,
 * user.avatarUrl, etc.) and call `getFileViewUrlApi` if you ever need a
 * short-lived signed URL outside of the normal entity-fetch flow — most
 * pages will get one for free in the entity's `*ViewUrl` field.
 *
 * @param filePath local file path (from uni.chooseFile / chooseImage)
 * @param folder   OSS folder prefix (default: `resumes`)
 * @returns OSS object key
 */
export const uploadFileApi = (filePath: string, folder: string = 'resumes'): Promise<string> => {
  return new Promise((resolve, reject) => {
    const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
    const token = uni.getStorageSync('token');

    uni.uploadFile({
      url: `${BASE_URL}/api/files/upload`,
      filePath: filePath,
      name: 'file',
      formData: { folder },
      header: {
        Authorization: token ? `Bearer ${token}` : '',
        // Required when the backend is behind ngrok-free.dev (otherwise we
        // get the abuse interstitial HTML instead of the JSON envelope).
        'ngrok-skip-browser-warning': '1',
      },
      success: (res) => {
        if (res.statusCode === 200) {
          try {
            const body = JSON.parse(res.data);
            if (body.code === 200) {
              resolve(body.data);
            } else {
              reject(new Error(body.message || 'Upload failed'));
            }
          } catch {
            reject(new Error('Invalid upload response'));
          }
        } else {
          reject(new Error('Upload failed'));
        }
      },
      fail: (err) => reject(err),
    });
  });
};

