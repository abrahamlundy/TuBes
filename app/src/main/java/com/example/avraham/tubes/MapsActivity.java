package com.example.avraham.tubes;

import android.*;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    public static final String jenis= "Pembeli";
    private static final int Request_of_Location =99 ;
    GoogleApiClient klien1;
    //Location lokasi_terakhir;
    double lat,lon;
    String userid;
    LocationRequest peminta_lokasi;
    TextView txt;
    //private Kontak_Server kontak;
    private JSONObject jsonObj;
    private String isi;

    protected synchronized void buildKlien(){
        klien1= new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void buatMintaLokasi(){
        peminta_lokasi = new LocationRequest();
        peminta_lokasi.setInterval(50000);
        peminta_lokasi.setFastestInterval(50000);
        peminta_lokasi.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buildKlien();
        //membuat si peminta_lokasi mulai minta-minta
        buatMintaLokasi();
    }

    public void ambilLokasi(){
        //ambil izin terlebih dahulu bila tidak aplikasi tidak berjalan semestinya
        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED)
        {
            //cara baca, periksa status akses izin objek ini untuk akses lokasi terbaik
            // bilamana tidak sama dengan Grant maka
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    Request_of_Location);

            // lanjutan cara baca pernyataan ini
             /*
             * maka tampilkan dialog permintaan izin secara manual untuk INI
             * dan kemudian hasil dari manifest yang terbaru disimpan dalam bentuk string
             * untuk disamakan dengan variabel field untuk lokasi
             * Ingat nilai hasil dari request ini bersifat integer
             * */
            return;
            //return tidak diberi jika hanya minta aplikasi menrupak peminta lokasi terakhir
            //namun bila sudah aware makan harus izinnya harus otomatis lempar biar semua sistem p
            //pada tau, dan ga minta lebih dari satu kali
        }

        //jika izin sudah selesai maka kita tidaka aakan ada hambatan untuk meyelesaikan yang lain

        //ambil lokasi yang sebenarnya
        //lokasi_terakhir= LocationServices.FusedLocationApi.getLastLocation(klien1);
         /*
         *  Cara baca lokasi_terakhir bernilai sama dengan nilai dari
         *  layanan lokasi dengan APILOKASI untuk lokasi terakhir( klient1);
         * */
        Log.i("fungsi","Fusi Lokasi");
        LocationServices.FusedLocationApi.requestLocationUpdates(klien1,peminta_lokasi,this);
         /*if(lokasi_terakhir!=null){
             Log.i("Latitude",String.valueOf(lokasi_terakhir.getLatitude()));
             Log.i("Longitude",String.valueOf(lokasi_terakhir.getLongitude()));
         }
         *
         */
    }

    public void onRequestPermissionResult(int requestCode, String permission[], int[] grantResults){
        if (requestCode == Request_of_Location){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.i("fungsi","onRequestPermissionResult");
                ambilLokasi();
            }else{
                AlertDialog ad = new AlertDialog.Builder(this).create();
                ad.setMessage("Tanpa Izin");
                ad.show();
            }
            return;
        }
    }

    public void onStart(){
        super.onStart();
        klien1.connect();
    }

    public void onStop(){
        klien1.disconnect();
        super.onStop();
    }

    private class SendData extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strUrl) {

            Log.v("yw","mulai ambil data");
            String hasil="";
            InputStream inStream=null;
            int len=500;  //ini adalah besaran untuk buffernya

            try{
                URL url = new URL(strUrl[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //timeout
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);

                conn.setRequestMethod("GET");
                conn.connect();

                int response = conn.getResponseCode();
                inStream = conn.getInputStream();

                //konversi stream ke string
                Reader r = null;
                r = new InputStreamReader(inStream,"UTF-8");
                char[] buffer = new char[len];
                r.read(buffer);
                hasil= new String(buffer);

            }catch(MalformedURLException e){
                e.printStackTrace();

            }catch (IOException e){
                e.printStackTrace();
            }finally {
                if(inStream!= null){
                    try{
                        inStream.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }

            return hasil;
        }

        protected void onPostExecute(String result){
            //tvHasil.setText(result);
            isi=result;
            try {
                jsonObj= new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            /*
            try {
                lat = jsonObj.getString("latitude");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                lon = jsonObj.getString("longitude");
            } catch (JSONException e) {
                e.printStackTrace();
            }*
            */

            //tvHasil.setText(lat+"and"+lon);
        }
    }

    private class AmbilData extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strUrl) {

            //Log.v("yw","mulai ambil data");
            String hasil="";
            InputStream inStream=null;
            int len=500;  //ini adalah besaran untuk buffernya

            try{
                URL url = new URL(strUrl[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //timeout
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);

                conn.setRequestMethod("GET");
                conn.connect();

                int  response = conn.getResponseCode();
                inStream = conn.getInputStream();

                //konversi stream ke string
                Reader r = null;
                r = new InputStreamReader(inStream,"UTF-8");
                char[] buffer = new char[len];
                r.read(buffer);
                hasil= new String(buffer);

            }catch(MalformedURLException e){
                e.printStackTrace();

            }catch (IOException e){
                e.printStackTrace();
            }finally {
                if(inStream!= null){
                    try{
                        inStream.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }

            return hasil;
        }

        protected void onPostExecute(String result){
            //tvHasil.setText(result);
            isi= result;
            try {
                jsonObj= new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                userid = jsonObj.getString("userid");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                lat = Double.parseDouble(jsonObj.getString("latitude"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                lon = Double.parseDouble(jsonObj.getString("longitude"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng me = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(me).title("Me"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(me));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("fungsi","onConnected");
        ambilLokasi();
         /*
         *panggil fungsi ambil lokasi ketika aplikasi dalam keadaan terhubung
         *fungsi ambil lokasi juga bisa dijabarkan secara manual disini
         */
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Toast t = Toast.makeText(getApplicationContext(),"Lokasi Berubah !", Toast.LENGTH_LONG);
        Log.i("statusarah","berubah");
        t.show();
        LatLng me = new LatLng(location.getLatitude(), location.getLongitude());
        LatLng penjual = new LatLng(lat,lon);
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo= connMgr.getActiveNetworkInfo();

        if(netinfo!=null && netinfo.isConnected()){
            new SendData().execute("http://139.59.103.113/updatelokasi.py?lat="+location.getLatitude()+"&long="+location.getLongitude()+"&tag=1407229&userid=pembeli08");
            new AmbilData().execute("http://139.59.103.113/getlokasimultiuser.py?tag=yw&userid=penjual%25");
        }else{
            t = Toast.makeText(getApplicationContext(),"Tidak ada koneksi !", Toast.LENGTH_LONG);
            t.show();
        }
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(me).title("Me").draggable(true));
        //mMap.addMarker(new MarkerOptions().position(penjual).title("Mang Bakso").draggable(true));
        mMap.moveCamera(CameraUpdateFactory.zoomIn());



    }
}
