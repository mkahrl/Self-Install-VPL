/* sample code
for reference only
No warranties implied or other.
Use at your own risk,
For instructional purposes only
 */


package ptt.fake.marks.vpldemo;

import android.app.Dialog;
import android.content.Intent;
import android.content.ContentResolver;
import android.net.Uri;
import java.net.URL;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.app.Activity;
import android.os.AsyncTask;
import android.provider.Settings.Secure;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import android.widget.ProgressBar;

public class MainActivity extends Activity implements View.OnClickListener
{
    ProgressBar progBar;
    int filesize=0;
    int totalBytesRead = 0;
    Dialog downloadFail;
    Dialog downloadSuccess;
    Dialog unknownSources;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        int unknownSources = 0;
        ContentResolver cr = this.getContentResolver();

        try {
            unknownSources = Secure.getInt(cr, Secure.INSTALL_NON_MARKET_APPS);
        }
        catch(Exception e){}

        if (unknownSources < 1) unkownSources();
        else
        {
            new GetApkFromServer().execute(null);
            progBar = (ProgressBar) findViewById(R.id.progress_bar);
            progBar.setMax(100);

        }
    }

    public void onClick(View v)
    {
        int id = v.getId();
        if ( id == R.id.ok_quit) downloadSuccess.dismiss();
        if ( id == R.id.fail_quit)
        {
            downloadFail.dismiss();
            finish();
        }
        if ( id == R.id.unkown_src_quit)
        {
            unknownSources.dismiss();
            finish();
        }

    }

    class GetApkFromServer extends AsyncTask
    {
        boolean apkRecieved = false;
        File localFile;

        public GetApkFromServer()
        {

        }

        @Override
        protected void onProgressUpdate(Object[] o)
        {
            progBar.setProgress((totalBytesRead*100 / filesize));
            progBar.invalidate();
            System.out.println("Progress ..... " + (totalBytesRead * 100 / filesize));
        }

        @Override
        protected Object doInBackground(Object[] o)
        {
            getApkFromServer();
            return null;
        }

        @Override
        protected void onPostExecute(Object result)
        {
            if (apkRecieved)
            {
                installAPK(localFile);
            }
            else apkDownloadFail();
        }

        void getApkFromServer()
        {
            System.out.println("getApkFromServer()");
            localFile = new File("/mnt/sdcard/" , "vpl.apk");

                try {
                    /// url of full featured live app, with same package and ap name, higher version code
                    URL url = new URL("http://kahrlconsulting.com/live-app.apk");
                    HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                    InputStream in = huc.getInputStream();

                    filesize = huc.getContentLength();
                    FileOutputStream localFileOutputStream = new FileOutputStream(localFile);
                    byte[] arrayOfByte = new byte[2048];

                    while (true)
                    {
                        int input = (in.read(arrayOfByte));

                        if (input < 1) break;
                        else
                        {
                            System.out.println("Read:  "+input+"  bytes ..");
                            totalBytesRead += input;
                            publishProgress(null);
                        }

                        localFileOutputStream.write(arrayOfByte, 0, input);
                    }
                    in.close();
                    localFileOutputStream.close();
                    apkRecieved = (filesize == totalBytesRead);

                } catch (Exception e) {
                    Log.e("MainActivity", "IOException..." + e.toString());
                    e.printStackTrace();
                }
        }

    }

    private void installAPK(File apk)
    {
        Intent install_intent = new Intent(Intent.ACTION_VIEW);
        install_intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
        startActivity(install_intent);
    }

    private void apkDownloadSuccess()
    {
        System.out.println("apkDownloadSuccess() ...");
        downloadSuccess = new Dialog(MainActivity.this);
        downloadSuccess.setContentView(R.layout.file_download_complete);
        downloadSuccess.findViewById(R.id.ok_quit).setOnClickListener(this);
        downloadSuccess.show();
    }

    private void apkDownloadFail()
    {
        System.out.println("apkDownloadFail() ...");
        downloadFail = new Dialog(MainActivity.this);
        downloadFail.setContentView(R.layout.file_download_fail);
        downloadFail.findViewById(R.id.fail_quit).setOnClickListener(this);
        downloadFail.show();
    }

    private void unkownSources()
    {
        unknownSources = new Dialog(MainActivity.this);
        unknownSources.setContentView(R.layout.unknown_sources_check);
        unknownSources.findViewById(R.id.unkown_src_quit).setOnClickListener(this);
        unknownSources.show();
    }
}

