package com.example.translatorapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private OkHttpClient client = new OkHttpClient();

     private Spinner fromspinner;
     private Spinner tospinner;
     private ImageButton mic;
     private EditText srctext;
     private TextView transtxt;
     private Button translatebtn;

    String[] fromLanguage =  {"From","English", "Sinhala"};

    String[] toLanguage =  {"To","English", "Sinhala"};

    private  static final int  REQUEST_PERMISSION_CODE = 1;
    String languagecode,fromLanguagecode,tolanguagecode ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       fromspinner = findViewById(R.id.fromspinner);
       tospinner = findViewById(R.id.tospinner);
       mic = findViewById(R.id.mic);
       srctext = findViewById(R.id.srctxt);
       transtxt = findViewById(R.id.translatedtxt);
       translatebtn = findViewById(R.id.translatebtn);


       //From spinner Creation
      fromspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

              fromLanguagecode = getlanguagecode(fromLanguage[i]);

          }

          @Override
          public void onNothingSelected(AdapterView<?> adapterView) {

          }
      });

        ArrayAdapter fromadapter = new ArrayAdapter(this,R.layout.spinner_item,fromLanguage);
        fromadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromspinner.setAdapter(fromadapter);

      //To Spinner creation
        tospinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tolanguagecode = getlanguagecode(toLanguage[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

         ArrayAdapter toadapter = new ArrayAdapter(this,R.layout.spinner_item,toLanguage);
         toadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         tospinner.setAdapter(toadapter);


      //creating translation button
        translatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            transtxt.setText("");
            if(srctext.getText().toString().isEmpty()){
                Toast.makeText(MainActivity.this, "Enter Your Text Here", Toast.LENGTH_SHORT).show();
            } else if (fromLanguagecode == null) {
                Toast.makeText(MainActivity.this, "Please Select Source Language", Toast.LENGTH_SHORT).show();
            } else if (tolanguagecode == null) {
                Toast.makeText(MainActivity.this, "Select the language to be translated", Toast.LENGTH_SHORT).show();
            }
            else {
                translateText(fromLanguagecode,tolanguagecode,srctext.getText().toString());

            }
            }
        });

       //Creating Mic
        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i  = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to convert into text");
                try {
                    startActivityForResult(i,REQUEST_PERMISSION_CODE);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }



            }
        });







    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_PERMISSION_CODE){
            if(resultCode == RESULT_OK && data!= null){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                srctext.setText(result.get(0));

            }        }


    }
    private String getlanguagecode(String language) {

        switch (language) {
            case "English":
                return "en";
            case "Sinhala":
                return "si";
            // Add more cases for other languages
            default:
                return null;
        }

    }

    private void translateText(String fromLanguagecode,String tolanguagecode, String textToTranslate) {
         MediaType mediaType = MediaType.parse("application/json");
        String requestBody = "{\n" +
                "    \"from\": \""+fromLanguagecode+"\",\n" +
                "    \"to\": \""+tolanguagecode+"\",\n" +
                "    \"q\": \"" + textToTranslate + "\"\n" +
                "}";
        Request request = new Request.Builder()
                .url("https://rapid-translate-multi-traduction.p.rapidapi.com/t")
                .post(RequestBody.create(mediaType, requestBody))
                .addHeader("content-type", "application/json")
                .addHeader("X-RapidAPI-Key", "f12c6f2b4dmshf4b328712b6bc9fp1d8e48jsnfb5fd217c3e1")
                .addHeader("X-RapidAPI-Host", "rapid-translate-multi-traduction.p.rapidapi.com")
                .build();



        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

         @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body().string();
                // Parse the JSON response and get the translated text
                // Assuming the response format is like: {"t":"Translated Text"}
                String translated = responseBody.substring(responseBody.indexOf(":") + 3, responseBody.lastIndexOf("\""));
                runOnUiThread(() -> transtxt.setText(translated));
            }







        });
    }
}