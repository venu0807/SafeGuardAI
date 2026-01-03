package com.example.android.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.example.android.R;
import com.example.android.adapters.EmergencyContactsAdapter;
import com.example.android.models.EmergencyContact;
import com.example.android.utils.EmergencyHelper;

import java.util.List;

public class EmergencyContactsActivity extends AppCompatActivity {

    private static final int PICK_CONTACT_REQUEST = 1;
    private static final int PERMISSION_READ_CONTACTS = 100;

    private RecyclerView recyclerView;
    private EmergencyContactsAdapter adapter;
    private View emptyView; // Changed from TextView to View to support the new LinearLayout structure
    private ExtendedFloatingActionButton fabAdd; // Updated to match the new Material 3 UI
    private AlertDialog activeDialog; 

    private List<EmergencyContact> contacts;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contacts);

        setupToolbar();
        initializeViews();
        loadContacts();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Emergency Contacts");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.rv_contacts);
        emptyView = findViewById(R.id.empty_view); // This is now a LinearLayout in XML
        fabAdd = findViewById(R.id.fab_add_contact);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadContacts() {
        try {
            contacts = EmergencyHelper.getEmergencyContacts(this);
            if (contacts == null) {
                contacts = new java.util.ArrayList<>();
            }
            adapter = new EmergencyContactsAdapter(contacts, new EmergencyContactsAdapter.ContactClickListener() {
                @Override
                public void onEditClick(int position) { 
                    if (position >= 0 && position < contacts.size()) {
                        showEditContactDialog(position);
                    }
                }
                @Override
                public void onDeleteClick(int position) { 
                    if (position >= 0 && position < contacts.size()) {
                        showDeleteConfirmationDialog(position);
                    }
                }
                @Override
                public void onCallClick(int position) { 
                    if (position >= 0 && position < contacts.size()) {
                        makePhoneCall(contacts.get(position).getPhoneNumber());
                    }
                }
            });
            recyclerView.setAdapter(adapter);
            updateEmptyView();
        } catch (Exception e) {
            android.util.Log.e("EmergencyContacts", "Error loading contacts", e);
        }
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> showAddContactOptions());
    }

    private void showAddContactOptions() {
        dismissActiveDialog();
        activeDialog = new AlertDialog.Builder(this)
                .setTitle("Add Contact")
                .setItems(new String[]{"Manual Entry", "Select from Contacts"}, (d, w) -> {
                    if (w == 0) showAddContactDialog(); else pickContactFromPhoneBook();
                }).show();
    }

    private void showAddContactDialog() {
        dismissActiveDialog();
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null);
        EditText nameIn = view.findViewById(R.id.input_name);
        EditText phoneIn = view.findViewById(R.id.input_phone);
        EditText relIn = view.findViewById(R.id.input_relationship);

        activeDialog = new AlertDialog.Builder(this)
                .setTitle("Add Emergency Contact").setView(view)
                .setPositiveButton("Add", (d, w) -> {
                    String n = nameIn.getText().toString().trim();
                    String p = phoneIn.getText().toString().trim();
                    String r = relIn.getText().toString().trim();
                    if (validateContact(n, p)) {
                        EmergencyContact contact = new EmergencyContact(n, p, r);
                        if (EmergencyHelper.addEmergencyContact(this, contact)) {
                            contacts.add(contact);
                            adapter.notifyDataSetChanged();
                            updateEmptyView();
                        }
                        dismissActiveDialog();
                    }
                }).setNegativeButton("Cancel", (d, w) -> dismissActiveDialog()).show();
    }

    private void showEditContactDialog(int position) {
        dismissActiveDialog();
        EmergencyContact c = contacts.get(position);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null);
        EditText nameIn = view.findViewById(R.id.input_name);
        EditText phoneIn = view.findViewById(R.id.input_phone);
        EditText relIn = view.findViewById(R.id.input_relationship);

        nameIn.setText(c.getName());
        phoneIn.setText(c.getPhoneNumber());
        relIn.setText(c.getRelationship());

        activeDialog = new AlertDialog.Builder(this)
                .setTitle("Edit Contact").setView(view)
                .setPositiveButton("Save", (d, w) -> {
                    String n = nameIn.getText().toString().trim();
                    String p = phoneIn.getText().toString().trim();
                    String r = relIn.getText().toString().trim();
                    if (validateContact(n, p)) {
                        c.setName(n);
                        c.setPhoneNumber(p);
                        c.setRelationship(r);
                        if (EmergencyHelper.saveEmergencyContacts(this, contacts)) {
                            adapter.notifyItemChanged(position);
                        }
                        dismissActiveDialog();
                    }
                }).setNegativeButton("Cancel", (d, w) -> dismissActiveDialog()).show();
    }

    private void showDeleteConfirmationDialog(int position) {
        dismissActiveDialog();
        activeDialog = new AlertDialog.Builder(this)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete this contact?")
                .setPositiveButton("Delete", (d, w) -> {
                    contacts.remove(position);
                    if (EmergencyHelper.saveEmergencyContacts(this, contacts)) {
                        adapter.notifyDataSetChanged();
                        updateEmptyView();
                    }
                }).setNegativeButton("Cancel", (d, w) -> dismissActiveDialog()).show();
    }

    private void pickContactFromPhoneBook() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_READ_CONTACTS);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            if (contactUri == null) return;

            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                
                if (nameIdx != -1 && numberIdx != -1) {
                    String name = cursor.getString(nameIdx);
                    String number = cursor.getString(numberIdx);
                    showAddContactWithDetails(name, number);
                }
                cursor.close();
            }
        }
    }

    private void showAddContactWithDetails(String name, String phone) {
        dismissActiveDialog();
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null);
        EditText nIn = view.findViewById(R.id.input_name);
        EditText pIn = view.findViewById(R.id.input_phone);
        EditText rIn = view.findViewById(R.id.input_relationship);
        nIn.setText(name);
        pIn.setText(phone);

        activeDialog = new AlertDialog.Builder(this)
                .setTitle("Add Contact").setView(view)
                .setPositiveButton("Add", (d, w) -> {
                    String finalName = nIn.getText().toString().trim();
                    String finalPhone = pIn.getText().toString().trim();
                    String finalRel = rIn.getText().toString().trim();
                    if (validateContact(finalName, finalPhone)) {
                        EmergencyContact contact = new EmergencyContact(finalName, finalPhone, finalRel);
                        if (EmergencyHelper.addEmergencyContact(this, contact)) {
                            contacts.add(contact);
                            adapter.notifyDataSetChanged();
                            updateEmptyView();
                        }
                        dismissActiveDialog();
                    }
                }).setNegativeButton("Cancel", (d, w) -> dismissActiveDialog()).show();
    }

    private boolean validateContact(String name, String phone) {
        if (name.isEmpty() || phone.length() < 8) {
            Toast.makeText(this, "Valid name and phone required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void makePhoneCall(String phoneNumber) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)));
        } else {
            Toast.makeText(this, "Calling permission required", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEmptyView() {
        if (emptyView != null) {
            emptyView.setVisibility(contacts.isEmpty() ? View.VISIBLE : View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(contacts.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void dismissActiveDialog() {
        if (activeDialog != null && activeDialog.isShowing()) {
            activeDialog.dismiss();
            activeDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        dismissActiveDialog();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_READ_CONTACTS && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickContactFromPhoneBook();
        }
    }
}
