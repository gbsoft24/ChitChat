package com.gbsoft.chitchat.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.gbsoft.chitchat.R;
import com.gbsoft.chitchat.helper.KeysAndConstants;
import com.gbsoft.chitchat.helper.Utils;
import com.gbsoft.chitchat.model.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class ChatActivity extends AppCompatActivity {

    private ImageButton btnSend, btnImgSend;
    private EditText edtMsgCon;
    private RecyclerView messageListRecView;
    private StorageReference storageReference;
    private FirebaseRecyclerAdapter adapter;
    private FirebaseRecyclerOptions<Message> options;
    private String currUsrEmail, currUsrName;
    private Uri photoUrl;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currUsrEmail = getIntent().getStringExtra("currentUser");

        if(TextUtils.isEmpty(currUsrEmail)){
            Toast.makeText(this, "You need to login or signup to continue", Toast.LENGTH_LONG).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        }else{
            currUsrName = SignupActivity.emailTrimmer(currUsrEmail);
        }
        databaseReference = Utils.getmDatabaseRef();

        storageReference = FirebaseStorage.getInstance().getReference();

        // code for initializing the recycler view
        messageListRecView = findViewById(R.id.recyclerView);
        messageListRecView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messageListRecView.setLayoutManager(linearLayoutManager);

        Query query =   databaseReference.child("messages").limitToLast(10);

        options = new FirebaseRecyclerOptions.Builder<Message>().setQuery(query, Message.class).build();

        adapter = new FirebaseRecyclerAdapter<Message, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Message model) {
                Log.d("TAG", "I am from inside the onBindViewHolder method and getCount() returns: "+ adapter.getItemCount());
                String senderName = model.getUserName();
                Log.d("usrName:", " usrname from model "+model.getUserName() +
                        " and from loginactivity "+ currUsrName);
               if(!TextUtils.isEmpty(senderName) && !TextUtils.isEmpty(currUsrEmail)){
                   if(!(model.getUserName().equals(currUsrName))){
                       holder.thisRow.setGravity(Gravity.END);
                       holder.thisRow.setBackground(getResources().getDrawable(R.drawable.message_shape_other));
                   }
               }else{

                   Log.d("usrName:", " usrname from model "+ model.getUserName() +
                           " and from loginactivity "+ currUsrName);
                   holder.thisRow.setGravity(Gravity.START);
                   holder.thisRow.setBackground(getResources().getDrawable(R.drawable.message_shape_this));
               }
             holder.txtUsrName.setText(senderName);
             holder.txtTime.setText(model.getDateSent());
             holder.txtMsgCon.setText(model.getMessageContent());

             if(!TextUtils.isEmpty(model.getPhotoUrl())){
                holder.imgMsgCon.setVisibility(View.VISIBLE);
                 Bitmap bitmap = null;
                 try {
                      bitmap = new DownloadTask().execute(model.getPhotoUrl()).get();
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 } catch (ExecutionException e) {
                     e.printStackTrace();
                 }
                 holder.imgMsgCon.setImageBitmap(bitmap);
                Log.d("img", "holder should have been populated with image");
             }else{
                 holder.imgMsgCon.setVisibility(View.GONE);
                 Log.d("img", "image view has now gone");
             }
            }

            @Override
            public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                Log.d("TAG", "I am from inside the onCreateViewHolder method.");
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_row, parent, false);
                return new MyViewHolder(view);
            }
        };

        messageListRecView.setAdapter(adapter);

        edtMsgCon = findViewById(R.id.edtTxtMsg);
        btnSend = findViewById(R.id.imgBtnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageId = databaseReference.child("messages").push().getKey();
                String photoStringUrl;
                if(photoUrl == null){
                    photoStringUrl = "";
                }else{
                    photoStringUrl = photoUrl.toString();
                }
                Message newMsg = new Message(currUsrName, edtMsgCon.getText().toString(), getDate(), messageId, photoStringUrl);
                databaseReference.child("messages").child(messageId).setValue(newMsg);
                edtMsgCon.setText("");
                messageListRecView.scrollToPosition(adapter.getItemCount());
                photoUrl = null;
                Log.d("TAG", "I am the last statement of btnSend onClick handler.");
            }
        });

        btnImgSend = findViewById(R.id.imgBtnSendImg);
        btnImgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), 10);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 10 && resultCode == RESULT_OK){
            if(data.getData() != null){
                try {
                    confirmImgUploadDialog(data.getData());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menuSignOut) {
            if(!TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                Utils.getmAuth().signOut();
                databaseReference.child("onlineUsrs").child(Utils.getmAuth().getUid()).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError == null){

                        }
                    }
                });
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static boolean checkInternetConnection(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if(manager != null){
            activeNetworkInfo = manager.getActiveNetworkInfo();
        }
        return (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting());
    }

    public static void showAlertDialog(final Activity context){
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("Please Wait!!")
                .setMessage("Please check your internet connection to continue.")
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        context.finish();
                    }})
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        dialog = builder.create();
        dialog.show();
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtUsrName, txtMsgCon, txtTime;
        private ImageView imgMsgCon;
        private RelativeLayout thisRow;
        private MyViewHolder(View view) {
            super(view);
            txtUsrName = view.findViewById(R.id.txtUsrName);
            txtMsgCon = view.findViewById(R.id.txtMsgCon);
            txtTime = view.findViewById(R.id.txtTime);
            thisRow = view.findViewById(R.id.rowRelLayout);
            imgMsgCon = view.findViewById(R.id.imgMsgCon);
        }
    }

    private String getDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
        return simpleDateFormat.format(Calendar.getInstance().getTime());
    }

    private static class DownloadTask extends AsyncTask<String, Void, Bitmap> {
        private Bitmap bmp;
        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap bitmap = null;
            String photoUrl = urls[0];
            try{
                InputStream is = new java.net.URL(photoUrl).openStream();
                bitmap = BitmapFactory.decodeStream(is);
                Log.d("downloadtask", "Photo has been downloaded.");
            }catch (Exception e){
                Log.d("downloadtask", e.getMessage());
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
        }
    }

    public void confirmImgUploadDialog(final Uri uri){
        final AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.confirm_img_upload, null, false);
        builder.setView(dialogView);
        ImageButton imgBtnSend = dialogView.findViewById(R.id.dialogImgBtnSend);
        ImageView img = dialogView.findViewById(R.id.dialogImgView);
        img.setImageURI(uri);
        dialog = builder.create();
        imgBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uri != null){
                    storageReference.child(new File(uri.toString()).getName()).putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                Log.d("imgupload", "image has been uploaded successfully.");
                                photoUrl = task.getResult().getDownloadUrl();
                                Log.d("imgupload", "image url is :" + photoUrl.toString());


                                // send the message also

                                String messageId = databaseReference.child("messages").push().getKey();
                                String photoStringUrl;
                                if(photoUrl == null){
                                    photoStringUrl = "";
                                }else{
                                    photoStringUrl = photoUrl.toString();
                                }
                                Message newMsg = new Message(currUsrName, "", getDate(), messageId, photoStringUrl);
                                databaseReference.child("messages").child(messageId).setValue(newMsg);
                                edtMsgCon.setText("");
                                messageListRecView.scrollToPosition(adapter.getItemCount());
                                photoUrl = null;
                            }else{
                                Toast.makeText(ChatActivity.this, "There has been some error uploading the pics",
                                        Toast.LENGTH_LONG).show();
                                Log.d("errorimgupload", task.getException().getMessage());
                            }
                        }
                    });
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}
