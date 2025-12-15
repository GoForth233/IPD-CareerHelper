import request from '@/utils/request';

/**
 * Upload File API (to OSS)
 * @param filePath Local file path (from uni.chooseFile)
 * @param folder Optional folder name (default: resumes)
 */
export const uploadFileApi = (filePath: string, folder: string = 'resumes'): Promise<string> => {
  return new Promise((resolve, reject) => {
    const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
    const token = uni.getStorageSync('token');
    
    uni.uploadFile({
      url: `${BASE_URL}/api/files/upload`,
      filePath: filePath,
      name: 'file',
      formData: {
        folder: folder // Add folder parameter
      },
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (uploadFileRes) => {
        if (uploadFileRes.statusCode === 200) {
          try {
            // Backend returns Result<String>
            const data = JSON.parse(uploadFileRes.data);
            if (data.code === 200) {
              resolve(data.data); // Return the OSS URL
            } else {
              reject(new Error(data.message));
            }
          } catch (e) {
            reject(new Error('Parse Error'));
          }
        } else {
          reject(new Error('Upload Failed'));
        }
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
};

