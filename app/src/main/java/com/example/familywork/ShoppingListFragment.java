package com.example.familywork;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// דף רשימת הקניות - כאן מנהלים את המוצרים שצריך לקנות, כולל תמונות והקראה קולית
public class ShoppingListFragment extends Fragment {

    // קודים לזיהוי חזרה ממצלמה או מגלריה
    private static final int REQ_IMAGE = 1001;
    private static final int REQ_CAMERA = 1002;

    private RecyclerView recycler;
    private ShoppingListAdapter adapter;
    private List<Item> items;

    private FloatingActionButton fabAdd, fabReadAll;
    private TextToSpeech tts; // אובייקט להקראת טקסט בקול

    private DatabaseReference shoppingRef;
    private DatabaseReference historyRef;

    private String selectedImageBase64; // משתנה לשמירת התמונה כסטרינג לפני שליחה לענן
    private ImageView currentPreviewImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // טעינת עיצוב ה-XML של הדף
        return inflater.inflate(R.layout.fragment_shopping_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        // קישור רכיבי הממשק
        recycler = view.findViewById(R.id.recyclerShopping);
        fabAdd = view.findViewById(R.id.fabAdd);
        fabReadAll = view.findViewById(R.id.fabReadAll);

        items = new ArrayList<>();

        // אתחול המתאם עם פונקציות לחיצה: הקראה, עריכה ומחיקה/העברה להיסטוריה
        adapter = new ShoppingListAdapter(
                getContext(),
                items,
                (item, pos) -> onItemShortClick(item, pos),
                (item, pos) -> openEditDialog(item, pos),
                (item, pos) -> onItemLongClick(item, pos)
        );

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        // שליפת קוד המשפחה מהזיכרון המקומי כדי לגשת לנתונים הנכונים
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("app", android.content.Context.MODE_PRIVATE);
        String familyCode = prefs.getString("familyCode", "");

        if (familyCode.isEmpty()) {
            android.widget.Toast.makeText(getContext(), "שגיאה: אין קוד משפחה", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // הגדרת נתיבי ה-Firebase עבור רשימת הקניות וההיסטוריה של המשפחה
        shoppingRef = FirebaseDatabase.getInstance()
                .getReference("families")
                .child(familyCode)
                .child("shoppingList");

        historyRef = FirebaseDatabase.getInstance()
                .getReference("families")
                .child(familyCode)
                .child("history");

        // חיבור המאזינים לעדכון הנתונים בזמן אמת
        attachFirebaseListeners();

        // אתחול מנוע הדיבור והגדרתו לעברית
        tts = new TextToSpeech(getContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("he", "IL"));
            }
        });

        // כפתורים להוספת מוצר ולהקראת כל הרשימה ברצף
        fabAdd.setOnClickListener(v -> openAddDialog());
        fabReadAll.setOnClickListener(v -> readAllItems());
    }

    // פונקציה לפתיחת חלון הוספת מוצר חדש כולל אפשרות לתמונה
    private void openAddDialog() {

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_item, null);

        EditText inputName = dialogView.findViewById(R.id.inputName);
        EditText inputQuantity = dialogView.findViewById(R.id.inputQuantity);
        Button btnAddImage = dialogView.findViewById(R.id.btnAddImage);
        Button btnTakePhoto = dialogView.findViewById(R.id.btnTakePhoto);
        ImageView previewImage = dialogView.findViewById(R.id.previewImage);

        selectedImageBase64 = null;
        previewImage.setVisibility(View.GONE);
        currentPreviewImage = previewImage;

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("הוספת מוצר")
                .setView(dialogView)
                .setPositiveButton("שמור", null)
                .setNegativeButton("ביטול", null)
                .create();

        dialog.show();

        // כפתור לבחירת תמונה קיימת מהגלריה
        btnAddImage.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, REQ_IMAGE);
        });

        // כפתור לצילום תמונה חדשה במצלמה
        btnTakePhoto.setOnClickListener(v -> {
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(i, REQ_CAMERA);
        });

        // לחיצה על "שמור" בדיאלוג - שליחת הנתונים ל-Firebase
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            String name = inputName.getText().toString().trim();
            String qtyStr = inputQuantity.getText().toString().trim();

            if (name.isEmpty() || qtyStr.isEmpty()) {
                Toast.makeText(getContext(), "נא למלא שם וכמות", Toast.LENGTH_SHORT).show();
                return;
            }

            int qty = Integer.parseInt(qtyStr);

            // יצירת פריט חדש עם מזהה ייחודי (Push)
            DatabaseReference ref = shoppingRef.push();
            Item item = new Item(ref.getKey(), name, qty, selectedImageBase64);
            ref.setValue(item);

            dialog.dismiss();
        });
    }

    // חלון לעריכת מוצר קיים (שם וכמות)
    private void openEditDialog(Item item, int position) {

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_item, null);

        EditText inputName = dialogView.findViewById(R.id.inputName);
        EditText inputQuantity = dialogView.findViewById(R.id.inputQuantity);
        ImageView previewImage = dialogView.findViewById(R.id.previewImage);

        // הצגת הנתונים הקיימים בדיאלוג
        inputName.setText(item.getName());
        inputQuantity.setText(String.valueOf(item.getQuantity()));

        // המרת התמונה מ-Base64 חזרה לביטמאפ כדי להציג בתצוגה מקדימה
        if (item.getImageBase64() != null) {
            byte[] bytes = Base64.decode(item.getImageBase64(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            previewImage.setImageBitmap(bitmap);
            previewImage.setVisibility(View.VISIBLE);
        } else {
            previewImage.setVisibility(View.GONE);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("עריכת מוצר")
                .setView(dialogView)
                .setPositiveButton("שמור", (d, w) -> {

                    String newName = inputName.getText().toString().trim();
                    String qtyStr = inputQuantity.getText().toString().trim();

                    if (newName.isEmpty() || qtyStr.isEmpty()) return;

                    int newQty = Integer.parseInt(qtyStr);

                    item.setName(newName);
                    item.setQuantity(newQty);

                    // עדכון הערכים ב-Firebase
                    shoppingRef.child(item.getId()).setValue(item);
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    // האזנה לשינויים ברשימה ב-Firebase (הוספה, שינוי, מחיקה)
    private void attachFirebaseListeners() {

        shoppingRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String s) {
                Item item = snapshot.getValue(Item.class);
                if (item != null) {
                    item.setId(snapshot.getKey());
                    items.add(item);
                    adapter.notifyItemInserted(items.size() - 1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String s) {
                Item updated = snapshot.getValue(Item.class);
                if (updated == null) return;

                String key = snapshot.getKey();
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).getId().equals(key)) {
                        updated.setId(key);
                        items.set(i, updated);
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String key = snapshot.getKey();
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).getId().equals(key)) {
                        items.remove(i);
                        adapter.notifyItemRemoved(i);
                        break;
                    }
                }

                // הודעה קטנה אם הרשימה התרוקנה
                if (items.isEmpty()) {
                    sendEmptyListNotification();
                }
            }

            private void sendEmptyListNotification() {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "הסל ריק", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, String s) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // קבלת התמונה שחזרה מהגלריה או מהמצלמה
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != getActivity().RESULT_OK || data == null) return;

        try {
            Bitmap bitmap = null;

            if (requestCode == REQ_IMAGE) {
                bitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().getContentResolver(),
                        data.getData()
                );
            }

            if (requestCode == REQ_CAMERA) {
                bitmap = (Bitmap) data.getExtras().get("data");
            }

            if (bitmap != null) {
                // המרת התמונה לסטרינג (Base64) כדי שנוכל לשמור אותה ב-Realtime Database
                selectedImageBase64 = bitmapToBase64(bitmap);
                currentPreviewImage.setImageBitmap(bitmap);
                currentPreviewImage.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // לחיצה קצרה על מוצר משמיעה את השם והכמות שלו
    private void onItemShortClick(Item item, int position) {
        tts.speak(item.getName() + " כמות " + item.getQuantity(),
                TextToSpeech.QUEUE_FLUSH, null, null);
    }

    // לחיצה ארוכה מאפשרת להעביר את המוצר להיסטוריית הקניות (מחיקה מהרשימה הנוכחית)
    private void onItemLongClick(Item item, int position) {
        new AlertDialog.Builder(getContext())
                .setTitle(item.getName())
                .setMessage("להעביר להיסטוריה?")
                .setPositiveButton("מחק", (d, w) -> {
                    // קבלת התאריך בפורמט לסינון (שנה-חודש-יום)
                    String todayDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                            .format(new java.util.Date());

                    // שמירה תחת תאריך היום בנתיב ההיסטוריה
                    historyRef.child(todayDate).push().setValue(item);

                    // מחיקה מרשימת הקניות הפעילה
                    shoppingRef.child(item.getId()).removeValue();
                })
                .setNegativeButton("בטל", null)
                .show();
    }

    // מעבר על כל הרשימה והקראתה ברצף
    private void readAllItems() {
        if (items.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        for (Item it : items) {
            sb.append(it.getName())
                    .append(" כמות ")
                    .append(it.getQuantity())
                    .append(". ");
        }

        tts.speak(sb.toString(), TextToSpeech.QUEUE_FLUSH, null, null);
    }

    // פונקציית עזר להמרת Bitmap לסטרינג (Base64) תוך דחיסת התמונה כדי שלא תהיה כבדה מדי
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    @Override
    public void onDestroyView() {
        // שחרור משאבים של מנוע הדיבור ביציאה מהמסך
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroyView();
    }
}
