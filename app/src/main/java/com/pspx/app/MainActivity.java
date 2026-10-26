package com.pspx.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.graphics.Color;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;

public class MainActivity extends Activity {
    private static final int PICK_GAME = 42;
    private LinearLayout games;
    private JSONArray saved;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(22, 32, 22, 18);
        root.setBackgroundColor(Color.rgb(16,20,32));
        TextView title = new TextView(this);
        title.setText("🎮 PSP X | الألعاب");
        title.setTextSize(25); title.setTextColor(Color.WHITE);
        root.addView(title);
        TextView note = new TextView(this);
        note.setText("5 ألعاب صغيرة تعمل الآن بدون إنترنت. قسم PSP لإضافة ملفات ألعابك؛ محرك PSP غير مدمج بعد.");
        note.setTextColor(0xffbbbbcc); note.setPadding(0,16,0,20);
        root.addView(note);
        String[] labels={"🐍 الثعبان", "🏓 كرة المضرب", "🚀 سفينة الفضاء", "🏎️ تفادي السيارات", "🎯 إصابة الهدف"};
        for(int n=0;n<labels.length;n++) {
            final int mode=n;
            Button b=new Button(this); b.setText(labels[n]); root.addView(b);
            b.setOnClickListener(v -> { Intent g=new Intent(this,MiniGameActivity.class);g.putExtra("mode",mode);startActivity(g); });
        }
        TextView section=new TextView(this);section.setText("📁 ملفات PSP (للمكتبة فقط)");section.setTextColor(Color.WHITE);section.setTextSize(18);root.addView(section);
        Button add = new Button(this); add.setText("+ إضافة لعبة"); root.addView(add);
        add.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.setType("*/*"); i.addCategory(Intent.CATEGORY_OPENABLE);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(i, PICK_GAME);
        });
        ScrollView scroll = new ScrollView(this);
        games = new LinearLayout(this); games.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(games); root.addView(scroll);
        setContentView(root);
        try { saved = new JSONArray(getPreferences(0).getString("games", "[]")); }
        catch(Exception e) { saved = new JSONArray(); }
        render();
    }
    private void render() {
        games.removeAllViews();
        for(int j=0; j<saved.length(); j++) {
            final int index=j;
            String uri=saved.optString(j);
            TextView row = new TextView(this);
            row.setText("🎮  " + fileName(Uri.parse(uri)) + "\nاضغط لعرض حالة التشغيل");
            row.setTextColor(Color.WHITE); row.setTextSize(17); row.setPadding(10,20,10,20);
            games.addView(row);
            row.setOnClickListener(v -> new android.app.AlertDialog.Builder(this)
                .setTitle("محرك التشغيل غير مدمج")
                .setMessage("اللعبة محفوظة في المكتبة، لكن تشغيل ISO/CSO يتطلب بناء ودمج محرك PPSSPP الأصلي مع واجهة JNI متوافقة.")
                .setPositiveButton("موافق",null)
                .setNegativeButton("حذف من المكتبة",(d,w)->remove(index)).show());
        }
    }
    private void remove(int index) {
        JSONArray next = new JSONArray();
        for(int k=0;k<saved.length();k++) if(k!=index) next.put(saved.optString(k));
        saved=next; persist(); render();
    }
    private void persist() { getPreferences(0).edit().putString("games",saved.toString()).apply(); }
    private String fileName(Uri uri) {
        try(Cursor c=getContentResolver().query(uri,null,null,null,null)) {
            if(c!=null && c.moveToFirst()) {
                int col=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if(col>=0) return c.getString(col);
            }
        } catch(Exception ignored) {}
        return uri.getLastPathSegment();
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request!=PICK_GAME || result!=RESULT_OK || data==null || data.getData()==null) return;
        Uri uri=data.getData();
        String name=fileName(uri).toLowerCase(java.util.Locale.ROOT);
        if(!(name.endsWith(".iso") || name.endsWith(".cso"))) {
            Toast.makeText(this,"اختار ملف ISO أو CSO",Toast.LENGTH_LONG).show(); return;
        }
        try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
        catch(Exception ignored) {}
        String val=uri.toString();
        for(int j=0;j<saved.length();j++) if(val.equals(saved.optString(j))) return;
        saved.put(val); persist(); render();
    }
}