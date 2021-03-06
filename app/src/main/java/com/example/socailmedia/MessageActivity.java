package com.example.socailmedia;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

public class MessageActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageView imageView;
    ImageButton sendbtn,cambtn,micbtn;
    TextView username;
    EditText messageEt;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference rootref1,rootref2;
    MessageMember messageMember;
    String  receiver_name,receiver_uid,sender_uid,url;

    Uri uri;
    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //to make secure from taking screenshot
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_message);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            url = bundle.getString("u");
            receiver_name = bundle.getString("n");
            receiver_uid = bundle.getString("uid");
        }else {
            Toast.makeText(this, "user missing", Toast.LENGTH_SHORT).show();
        }

        messageMember = new MessageMember();
        recyclerView = findViewById(R.id.rv_message);
        cambtn = findViewById(R.id.cam_sendmessage);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
        imageView = findViewById(R.id.iv_message);
        messageEt = findViewById(R.id.messageet);
        sendbtn = findViewById(R.id.imageButtonsend);
        username = findViewById(R.id.username_messageTv);

        Picasso.get().load(url).into(imageView);
        username.setText(receiver_name);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        sender_uid = user.getUid();

        rootref1 = database.getReference("Message").child(sender_uid).child(receiver_uid);
        rootref2 = database.getReference("Message").child(receiver_uid).child(sender_uid);

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

        cambtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,PICK_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == PICK_IMAGE || resultCode == RESULT_OK ||
                data != null || data.getData() != null) {
            uri = data.getData();

            String url = uri.toString();
            Intent intent = new Intent(MessageActivity.this,SendImage.class);
            intent.putExtra("u",url);
            intent.putExtra("n",receiver_name);
            intent.putExtra("ruid",receiver_uid);
            intent.putExtra("suid",sender_uid);
            startActivity(intent);
        }else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<MessageMember> options1 =
                new FirebaseRecyclerOptions.Builder<MessageMember>()
                        .setQuery(rootref1,MessageMember.class)
                        .build();

        FirebaseRecyclerAdapter<MessageMember,MessageViewHolder> firebaseRecyclerAdapter1 =
                new FirebaseRecyclerAdapter<MessageMember, MessageViewHolder>(options1) {
                    @Override
                    protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull MessageMember model) {

                        holder.Setmessage(getApplication(),model.getMessage(),model.getTime(),model.getDate(),model.getType(),
                                model.getSenderuid(),model.getReceiveruid());

                    }

                    @NonNull
                    @Override
                    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.message_layout,parent,false);

                        return new MessageViewHolder(view);
                    }
                };

        firebaseRecyclerAdapter1.startListening();
        recyclerView.setAdapter(firebaseRecyclerAdapter1);

    }

    private void SendMessage() {

        String message = messageEt.getText().toString();

        Calendar cdate = Calendar.getInstance();
        SimpleDateFormat currentdate = new SimpleDateFormat("dd-MMMM-yyyy");
        final  String savedate = currentdate.format(cdate.getTime());

        Calendar ctime = Calendar.getInstance();
        SimpleDateFormat currenttime = new SimpleDateFormat("HH:mm:ss");
        final String savetime = currenttime.format(ctime.getTime());

        String time = savedate +":"+ savetime;

        if (message.isEmpty()){
            Toast.makeText(this, "Cannot send empty message", Toast.LENGTH_SHORT).show();
        }else {
            messageMember.setDate(savedate);
            messageMember.setTime(savetime);
            messageMember.setMessage(message);
            messageMember.setReceiveruid(receiver_uid);
            messageMember.setSenderuid(sender_uid);
            messageMember.setType("text");

            String id = rootref1.push().getKey();
            rootref1.child(id).setValue(messageMember);

            String id1 = rootref2.push().getKey();
            rootref2.child(id1).setValue(messageMember);

            messageEt.setText("");


        }

    }


}













