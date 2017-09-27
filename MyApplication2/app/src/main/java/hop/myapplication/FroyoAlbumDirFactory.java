package hop.myapplication;

import android.os.Environment;

import java.io.File;

public final class FroyoAlbumDirFactory extends hop.myapplication.AlbumStorageDirFactory {

	@Override
	public File getAlbumStorageDir(String albumName) {
		// TODO Auto-generated method stub
		return new File(
		  Environment.getExternalStoragePublicDirectory(
		    Environment.DIRECTORY_PICTURES
		  ), 
		  albumName
		);
	}
}
