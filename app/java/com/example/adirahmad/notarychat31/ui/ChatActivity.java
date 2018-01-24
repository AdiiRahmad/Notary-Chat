package com.example.adirahmad.notarychat31.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.adirahmad.notarychat31.BuildConfig;
import com.example.adirahmad.notarychat31.Caesar;
import com.example.adirahmad.notarychat31.Converter;
import com.example.adirahmad.notarychat31.DES;
import com.example.adirahmad.notarychat31.EncodeDecodeAES;
import com.example.adirahmad.notarychat31.R;
import com.example.adirahmad.notarychat31.data.SharedPreferenceHelper;
import com.example.adirahmad.notarychat31.data.StaticConfig;
import com.example.adirahmad.notarychat31.model.Consersation;
import com.example.adirahmad.notarychat31.model.Message;
import com.example.adirahmad.notarychat31.util.Util;
import com.example.adirahmad.notarychat31.view.FullScreenImageActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView recyclerChat;
    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    public static final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    private ListMessageAdapter adapter;
    private String roomId;
    private ArrayList<CharSequence> idFriend;
    private Consersation consersation;
    private ImageButton btnSend;
    private EditText editWriteMessage;
    private LinearLayoutManager linearLayoutManager;
    public static HashMap<String, Bitmap> bitmapAvataFriend;
    public Bitmap bitmapAvataUser;

    public String mText = "";

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    FirebaseStorage storage = FirebaseStorage.getInstance();

    private static final int IMAGE_GALLERY_REQUEST = 1;
    private static final int IMAGE_CAMERA_REQUEST = 2;

    //File
    private File filePathImageCamera;

    static final String TAG = ChatActivity.class.getSimpleName();


    //Progress Upload
    private ProgressDialog mProgressDialog;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intentData = getIntent();
        idFriend = intentData.getCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID);
        roomId = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID);
        String nameFriend = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND);

        mProgressDialog = new ProgressDialog(this);



        consersation = new Consersation();
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        String base64AvataUser = SharedPreferenceHelper.getInstance(this).getUserInfo().avata;
        if (!base64AvataUser.equals(StaticConfig.STR_DEFAULT_BASE64)) {
            byte[] decodedString = Base64.decode(base64AvataUser, Base64.DEFAULT);
            bitmapAvataUser = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } else {
            bitmapAvataUser = null;
        }

        editWriteMessage = (EditText) findViewById(R.id.editWriteMessage);


        if (idFriend != null && nameFriend != null) {
            getSupportActionBar().setTitle(nameFriend);
            linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            recyclerChat = (RecyclerView) findViewById(R.id.recyclerChat);
            recyclerChat.setLayoutManager(linearLayoutManager);
            adapter = new ListMessageAdapter(this, consersation, bitmapAvataFriend, bitmapAvataUser);
            FirebaseDatabase.getInstance().getReference().child("message/" + roomId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    if (dataSnapshot.getValue() != null) {
                        HashMap mapMessage = (HashMap) dataSnapshot.getValue();
                        Message newMessage = new Message();
                        newMessage.idSender = (String) mapMessage.get("idSender");
                        newMessage.idReceiver = (String) mapMessage.get("idReceiver");
                        newMessage.text = (String) mapMessage.get("text");
                        newMessage.timestamp = (long) mapMessage.get("timestamp");
                        consersation.getListMessageData().add(newMessage);
                        adapter.notifyDataSetChanged();
                        linearLayoutManager.scrollToPosition(consersation.getListMessageData().size() - 1);

                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            recyclerChat.setAdapter(adapter);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        StorageReference storageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE).child(Util.FOLDER_STORAGE_IMG);

        if(requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                sendImageFirebase(storageRef, selectedImageUri);
            } else {
                //URI IS NULL
            }
        }else if (requestCode == IMAGE_CAMERA_REQUEST){
            if (resultCode == RESULT_OK){
                if (filePathImageCamera != null && filePathImageCamera.exists()){
                    StorageReference imageCameraRef = storageRef.child(filePathImageCamera.getName()+"_camera");
                    sendCameraFirebase(imageCameraRef,filePathImageCamera);
                }else{
                    //IS NULL
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }




    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Util.initToast(this,"Google Play Services error.");
    }




    public void clickImageChat(View view, int position,String nameUser,String urlPhotoUser,String urlPhotoClick) {
        Intent intent = new Intent(this,FullScreenImageActivity.class);
        intent.putExtra("nameUser",nameUser);
        intent.putExtra("urlPhotoUser",urlPhotoUser);
        intent.putExtra("urlPhotoClick",urlPhotoClick);
        startActivity(intent);
    }





    private void sendImageFirebase(StorageReference storageReference, final Uri file){
        if (storageReference != null){
            final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
            StorageReference imageGalleryRef = storageReference.child(name+"_gallery123456");
            UploadTask uploadTask = imageGalleryRef.putFile(file);
            mProgressDialog.setMessage("Mengirim Gambar dari Galeri Anda...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG,"onFailure sendFileFirebase "+e.getMessage());
                }
            })
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                String download_url = task.getResult().getDownloadUrl().toString();
                                Message newMessage = new Message();
                                try {
                                    Log.d ("Url Gambar","" +download_url);
                                    String caesar = new Caesar().enkripsiCaesar(download_url);
                                    Log.d ("Url Enkrip Caesar","" +caesar);

                                    newMessage.text = caesar;
                                    newMessage.idSender = StaticConfig.UID;
                                    newMessage.idReceiver = roomId;
                                    newMessage.timestamp = System.currentTimeMillis();
                                    FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage);
                                    mProgressDialog.dismiss();
                                    Toast.makeText(ChatActivity.this, "Kirim Gambar Berhasil", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {

                                }
                            }
                        }
                    });

        }else{
            //IS NULL
        }

    }

    private void sendCameraFirebase(StorageReference storageReference, final File file){
        if (storageReference != null){
            Uri photoURI = FileProvider.getUriForFile(ChatActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file);
            UploadTask uploadTask = storageReference.putFile(photoURI);
            mProgressDialog.setMessage("Mengupload Foto ...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG,"onFailure sendFileFirebase "+e.getMessage());
                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        String download_url = task.getResult().getDownloadUrl().toString();
                        Message newMessage = new Message();
                        try {
                            Log.d ("Url Gambar","" +download_url);
                            String caesar = new Caesar().enkripsiCaesar(download_url);
                            Log.d ("Url Enkrip Elgamal","" +caesar);

                            newMessage.text = caesar;
                            newMessage.idSender = StaticConfig.UID;
                            newMessage.idReceiver = roomId;
                            newMessage.timestamp = System.currentTimeMillis();
                            FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage);
                            mProgressDialog.dismiss();
                            Toast.makeText(ChatActivity.this, "Kirim Foto Berhasil", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {

                        }
                    }
                }
            });

        }else{
            //IS NULL
        }

    }



    private void photoCameraIntent(){
        String nomeFoto = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
        filePathImageCamera = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                , nomeFoto+"gallery");
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoURI = FileProvider.getUriForFile(ChatActivity.this,
                BuildConfig.APPLICATION_ID + ".provider",
                filePathImageCamera);
        it.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
        startActivityForResult(it, IMAGE_CAMERA_REQUEST);
    }


    private void photoGalleryIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_title)), IMAGE_GALLERY_REQUEST);
    }

    public void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(ChatActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    ChatActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }else{
            // we already have permission, lets go ahead and call camera intent
            photoCameraIntent();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Intent result = new Intent();
            result.putExtra("idFriend", idFriend.get(0));
            setResult(RESULT_OK, result);
            this.finish();
        }
        switch (item.getItemId()){
            case R.id.sendPhotoGallery:
                photoGalleryIntent();
                break;
            case R.id.sendCamera:
                verifyStoragePermissions();
//                photoCameraIntent();
                break;

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra("idFriend", idFriend.get(0));
        setResult(RESULT_OK, result);
        this.finish();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnSend) {
            String content = editWriteMessage.getText().toString().trim();
            if (content.length() > 0) {
                editWriteMessage.setText("");
                Message newMessage = new Message();
                try {
                    Log.d ("Pesan Awal ","" +content);
                    String Kunci = "MyNotaryChatApp"+roomId;
                    String EnkripAes = EncodeDecodeAES.encrypt(Kunci,content);
                    Log.d ("Enkrip AES " ,"Pesannya" +EnkripAes);
                    byte[] EnkripDES = DES.encrypt(Converter.static_stringToByteArray(EnkripAes),Converter.static_stringToByteArray((Kunci)));
                    String TextEnkripDES = Converter.static_byteArrayToString(EnkripDES);
                    Log.d ("Enkrip DES " ,"Pesannya" +TextEnkripDES);


                    newMessage.text = TextEnkripDES;
                    newMessage.idSender = StaticConfig.UID;
                    newMessage.idReceiver = roomId;
                    newMessage.timestamp = System.currentTimeMillis();
                    FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage);
                    Toast.makeText(ChatActivity.this, "Kirim Pesan Berhasil", Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    e.printStackTrace();
                }


            }
        }
    }
}

class ListMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private Consersation consersation;
    private HashMap<String, Bitmap> bitmapAvata;
    private HashMap<String, DatabaseReference> bitmapAvataDB;
    private Bitmap bitmapAvataUser;

    public ListMessageAdapter(Context context, Consersation consersation, HashMap<String, Bitmap> bitmapAvata, Bitmap bitmapAvataUser) {
        this.context = context;
        this.consersation = consersation;
        this.bitmapAvata = bitmapAvata;
        this.bitmapAvataUser = bitmapAvataUser;
        bitmapAvataDB = new HashMap<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ChatActivity.VIEW_TYPE_FRIEND_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_friend, parent, false);
            return new ItemMessageFriendHolder(view);
        } else if (viewType == ChatActivity.VIEW_TYPE_USER_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_user, parent, false);
            return new ItemMessageUserHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof ItemMessageFriendHolder) {

            if (consersation.getListMessageData().get(position).text.length()==169){

                ((ItemMessageFriendHolder) holder).ivContent.setVisibility(View.VISIBLE);
                ((ItemMessageFriendHolder) holder).txtContent.setVisibility(View.GONE);

                Glide.with(context).load(consersation.getListMessageData().get(position).text).
                        placeholder(R.drawable.bt_shape_2).error(R.drawable.bt_shape_2).
                        into(((ItemMessageFriendHolder) holder).ivContent);

                Log.d("TEST","TEST PRE");
                ((ItemMessageFriendHolder) holder).ivContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("TEST","TEST FRIEND");
                        Intent toFullScreen =   new Intent(context,FullScreenImageActivity.class);

                        String DekripCaesar = new Caesar().dekripsiCaesar(consersation.getListMessageData().get(position).text);
                        toFullScreen.putExtra("imgkey",DekripCaesar);
                        context.startActivity(toFullScreen);
                    }
                });

            }else {
                ((ItemMessageFriendHolder) holder).ivContent.setVisibility(View.GONE);
                ((ItemMessageFriendHolder) holder).txtContent.setVisibility(View.VISIBLE);

                String pesannya = consersation.getListMessageData().get(position).text;
                Log.d("Dari Firebase", "Chiper Text " +pesannya);
                String Kunci = "MyNotaryChatApp"+consersation.getListMessageData().get(position).idReceiver;
                Log.d("Kunci Pesan ", "kunci "+Kunci);
                byte[] DekripDES = DES.decrypt(Converter.static_stringToByteArray(pesannya), Converter.static_stringToByteArray(Kunci));
                String TextDekripDES = Converter.static_byteArrayToString(DekripDES);
                Log.d("Pesan Dekrip DES", "dekrip DES " +TextDekripDES);
                String DekripAES;
                try {
                    DekripAES = EncodeDecodeAES.decrypt(Kunci,TextDekripDES);
                    Log.d("Pesan Dekrip AES", "dekrip AES " +DekripAES);
                    ((ItemMessageFriendHolder) holder).txtContent.setText(DekripAES);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Bitmap currentAvata = bitmapAvata.get(consersation.getListMessageData().get(position).idSender);
            if (currentAvata != null) {
                ((ItemMessageFriendHolder) holder).avata.setImageBitmap(currentAvata);
            } else {
                final String id = consersation.getListMessageData().get(position).idSender;
                if(bitmapAvataDB.get(id) == null){
                    bitmapAvataDB.put(id, FirebaseDatabase.getInstance().getReference().child("user/" + id + "/avata"));
                    bitmapAvataDB.get(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                String avataStr = (String) dataSnapshot.getValue();
                                if(!avataStr.equals(StaticConfig.STR_DEFAULT_BASE64)) {
                                    byte[] decodedString = Base64.decode(avataStr, Base64.DEFAULT);
                                    ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                                }else{
                                    ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avata));
                                }
                                notifyDataSetChanged();
                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        }
        //User
        else if (holder instanceof ItemMessageUserHolder) {

            if (consersation.getListMessageData().get(position).text.length()==169){
                ((ItemMessageUserHolder) holder).ivContent.setVisibility(View.VISIBLE);
                ((ItemMessageUserHolder) holder).txtContent.setVisibility(View.GONE);

                Glide.with(context).load(consersation.getListMessageData().get(position).text).
                        placeholder(R.drawable.bt_shape_2).error(R.drawable.bt_shape_2).
                        into(((ItemMessageUserHolder) holder).ivContent);

                Log.d("TEST","TEST PRE USERRR");
                ((ItemMessageUserHolder) holder).ivContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("TEST2","TEST USER");
                        Intent toFullScreen =   new Intent(context,FullScreenImageActivity.class);

                        String DekripCaesar = new Caesar().dekripsiCaesar(consersation.getListMessageData().get(position).text);
                        toFullScreen.putExtra("imgkey",DekripCaesar);
                        context.startActivity(toFullScreen);
                    }
                });
            }else{
                ((ItemMessageUserHolder) holder).ivContent.setVisibility(View.GONE);
                ((ItemMessageUserHolder) holder).txtContent.setVisibility(View.VISIBLE);

                String pesannya = consersation.getListMessageData().get(position).text;
                Log.d("pesan dari Firebase", "Chiper Text "+pesannya);
                String Kunci = "MyNotaryChatApp"+consersation.getListMessageData().get(position).idReceiver;
                Log.d("Kunci Pesan Firebase", "kunci "+Kunci);
                byte[] DekripDES = DES.decrypt(Converter.static_stringToByteArray(pesannya), Converter.static_stringToByteArray(Kunci));
                String TextDekripDES = Converter.static_byteArrayToString(DekripDES);
                Log.d("Pesan Dekrip DES", "dekrip DES " + TextDekripDES);
                String DekripAES;
                try {
                    DekripAES = EncodeDecodeAES.decrypt(Kunci,TextDekripDES);
                    Log.d("Pesan Dekrip AES", "dekrip AES "+DekripAES);
                    ((ItemMessageUserHolder) holder).txtContent.setText(DekripAES);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (bitmapAvataUser != null) {
                ((ItemMessageUserHolder) holder).avata.setImageBitmap(bitmapAvataUser);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
//        return consersation.getListMessageData().get(position).
//                idSender.equals(StaticConfig.UID) ?
//                ChatActivity.VIEW_TYPE_USER_MESSAGE : ChatActivity.VIEW_TYPE_FRIEND_MESSAGE;
        if (consersation.getListMessageData().get(position).idSender.equals(StaticConfig.UID)){
            return ChatActivity.VIEW_TYPE_USER_MESSAGE ;
        }else {
            return ChatActivity.VIEW_TYPE_FRIEND_MESSAGE ;
        }
    }

    @Override
    public int getItemCount() {
        return consersation.getListMessageData().size();
    }
}

class ItemMessageUserHolder extends RecyclerView.ViewHolder {
    public TextView txtContent;
    public ImageView ivContent;
    public CircleImageView avata;


    public ItemMessageUserHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentUser);
        avata = (CircleImageView) itemView.findViewById(R.id.imageView2);
        ivContent   =   (ImageView)itemView.findViewById(R.id.ivImage);
        txtContent.setTextIsSelectable(true);
    }
}

class ItemMessageFriendHolder extends RecyclerView.ViewHolder {
    public TextView txtContent;
    public CircleImageView avata;
    public ImageView ivContent;


    public ItemMessageFriendHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentFriend);
        avata = (CircleImageView) itemView.findViewById(R.id.imageView3);
        ivContent   =   (ImageView)itemView.findViewById(R.id.ivImage);
        txtContent.setTextIsSelectable(true);
    }


}
