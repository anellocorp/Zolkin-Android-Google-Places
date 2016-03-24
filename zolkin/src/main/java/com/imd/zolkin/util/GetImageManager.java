package com.imd.zolkin.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetImageManager
{
	WeakReference<Activity> c;
	int maxSizeX, maxSizeY;
	String mCurrentPhotoPath;
	GetImageManagerResponse callback;

	private static final int REQ_CODE_CAMERA = 111;
	private static final int REQ_CODE_GALLERY = 222;
	public static final String TAG = "GetImageManager";

	public interface GetImageManagerResponse
	{
		public void gotImage(Bitmap bm);
		public void dispatchActivityForResult(Intent i, int reqCode);
	}

	public GetImageManager(Activity context, int maxSizeX, int maxSizeY, GetImageManagerResponse callbackIn)
	{
		this.c = new WeakReference<Activity>(context);
		this.callback = callbackIn;

		this.maxSizeX = maxSizeX;
		this.maxSizeY = maxSizeY;

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(c.get());
		alertDialogBuilder.setTitle("Image").setMessage("Do you want to get an image from the camera or the gallery?").setCancelable(false).setPositiveButton("Camera", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					dialog.cancel();
					Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					if (takePictureIntent.resolveActivity(c.get().getPackageManager()) != null)
					{
						// Create the File where the photo
						// should go
						File photoFile = null;
						try
						{
							photoFile = createImageFile();
						}
						catch (IOException ex)
						{
							// Error occurred while creating
							// the File
							Toast.makeText(c.get(), "Error. Can't create file for camera. " + ex.toString(), Toast.LENGTH_LONG).show();
						}
						// Continue only if the File was
						// successfully created
						if (photoFile != null)
						{
							takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
							if (callback != null)
							{
								callback.dispatchActivityForResult(takePictureIntent, REQ_CODE_CAMERA);
							}
							//c.get().startActivityForResult(takePictureIntent, REQ_CODE_CAMERA);
						}
					}
				}
			}).setNegativeButton("Gallery", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int id)
				{
					dialog.cancel();
					Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					if (callback != null)
					{
						callback.dispatchActivityForResult(i, REQ_CODE_GALLERY);
					}
					//c.get().startActivityForResult(i, REQ_CODE_GALLERY);
				}
			});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
			case REQ_CODE_CAMERA:
			{
				if (resultCode == Activity.RESULT_OK)
				{
					// Bundle extras = data.getExtras();
					// Bitmap imageBitmap = (Bitmap) extras.get("data");
					// imgvFoto.setImageBitmap(imageBitmap);
	
					mCurrentPhotoPath = mCurrentPhotoPath.replace("file:", "");
	
					// Get the dimensions of the View
					int targetW = maxSizeX;
					int targetH = maxSizeY;
	
					// Get the dimensions of the bitmap
					BitmapFactory.Options bmOptions = new BitmapFactory.Options();
					bmOptions.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
					int photoW = bmOptions.outWidth;
					int photoH = bmOptions.outHeight;
	
					// Determine how much to scale down the image
					int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
	
					// Decode the image file into a Bitmap sized to fill the View
					bmOptions.inJustDecodeBounds = false;
					bmOptions.inSampleSize = scaleFactor;
					bmOptions.inPurgeable = true;
	
					Bitmap bm = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
	
					// Bitmap bm = BitmapFactory.decodeFile(mCurrentPhotoPath);
	
					int orientation = getOrientationCamera(mCurrentPhotoPath);
	
					if (orientation > 0)
					{
						Matrix matrix = new Matrix();
						matrix.postRotate(orientation);
	
						bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
					}
	
					if (callback != null)
					{
						callback.gotImage(bm);
					}
				}
			}
				break;
			case REQ_CODE_GALLERY:
			{
				if (resultCode == Activity.RESULT_OK && data != null)
				{
					Uri selectedImage = data.getData();
					String[] filePathColumn = { MediaStore.Images.Media.DATA };
					Cursor cursor = c.get().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
					cursor.moveToFirst();
					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
					String picturePath = cursor.getString(columnIndex);
					cursor.close();
	
					if (picturePath.startsWith("http"))
					{
						Toast.makeText(c.get(), "Esta foto não esta local no aparelho. Por favor, escolha uma foto local", Toast.LENGTH_LONG).show();
						if (callback != null)
						{
							callback.gotImage(null);
						}
						return;
					}
	
					Bitmap bm = BitmapFactory.decodeFile(picturePath);
	
					int orientation = getOrientation(c.get(), selectedImage);
	
					if (orientation > 0)
					{
						Matrix matrix = new Matrix();
						matrix.postRotate(orientation);
	
						bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
					}
					int w = bm.getWidth();
					int h = bm.getHeight();
					while (w > maxSizeX || h > maxSizeY)
					{
						w = (int) (w * 0.9);
						h = (int) (h * 0.9);
					}
					bm = Bitmap.createScaledBitmap(bm, w, h, false);
					
					if (callback != null)
					{
						callback.gotImage(bm);
					}
				}
				else
				{
					Log.d(TAG, "Gallery returned null Intent data");
				}
			}
			break;
		} //end switch
	}

	public File createImageFile() throws IOException
	{
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(imageFileName, /* prefix */
				".jpg", /* suffix */
				storageDir /* directory */
		);

		// Save a file: path for use with ACTION_VIEW intents
		mCurrentPhotoPath = "file:" + image.getAbsolutePath();
		return image;
	}

	int getOrientationCamera(String path)
	{
		ExifInterface exif;
		try
		{
			exif = new ExifInterface(path);
			int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			if (rotation == ExifInterface.ORIENTATION_ROTATE_90)
			{
				return 90;
			}
			else if (rotation == ExifInterface.ORIENTATION_ROTATE_180)
			{
				return 180;
			}
			else if (rotation == ExifInterface.ORIENTATION_ROTATE_270)
			{
				return 270;
			}
			return 0;
		}
		catch (IOException e)
		{ // TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	int getOrientation(Context context, Uri photoUri)
	{
		Cursor cursor = context.getContentResolver().query(photoUri, new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

		try
		{
			if (cursor.moveToFirst())
			{
				return cursor.getInt(0);
			}
			else
			{
				return -1;
			}
		}
		finally
		{
			cursor.close();
		}
	}
}
