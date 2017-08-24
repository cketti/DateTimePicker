package com.takisoft.datetimepicker.sample;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.takisoft.datetimepicker.DatePickerDialog;
import com.takisoft.datetimepicker.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Calendar cal = Calendar.getInstance();

        String skeleton = "MMMMy";//"yyyyMMMdd";

        Locale locale = Locale.US;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            String originalPattern = DateFormat.getBestDateTimePattern(locale, skeleton);
            SimpleDateFormat sdf = new SimpleDateFormat(originalPattern, locale);
            Log.i("Result[ORIGINAL]", "" + originalPattern + ", date: " + sdf.format(cal.getTime()));
        }

        String newPattern = getBestDateTimePattern(locale, skeleton);
        SimpleDateFormat sdf = new SimpleDateFormat(newPattern, locale);
        Log.i("Result[CUSTOM]", "" + newPattern + ", date: " + sdf.format(cal.getTime()));


        findViewById(R.id.btnDateFragment)
                .setOnClickListener(view -> {
                            /*DatePickerDialog
                                    .newInstance(null, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE))
                                    .show(getFragmentManager(), null);*/
                            DatePickerDialog dpd = new DatePickerDialog(MainActivity.this, (view1, year, month, dayOfMonth) -> {
                                Toast.makeText(MainActivity.this, year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", dayOfMonth), Toast.LENGTH_SHORT).show();
                            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
                            dpd.show();
                        }
                );

        findViewById(R.id.btnTimeFragment)
                .setOnClickListener(view -> {
                        /*TimePickerDialog
                        .newInstance(null, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true)
                        .show(getFragmentManager(), null);*/
                            TimePickerDialog tpd = new TimePickerDialog(MainActivity.this, (view1, hourOfDay, minute) -> {
                                Toast.makeText(MainActivity.this, String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute), Toast.LENGTH_SHORT).show();
                            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
                            tpd.show();
                        }
                );
    }

    public static String getBestDateTimePattern(Locale locale, String skeleton) {
        String format;
        SimpleDateFormat simpleDateFormat = (SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.LONG, locale);
        format = simpleDateFormat.toLocalizedPattern();

        Log.d("Format", "localized pattern: " + format);
        Log.d("Format", "pattern: " + simpleDateFormat.toPattern());

        Set<Character> excludedChars = new HashSet<>();
        HashMap<Character, Integer> includedChars = new HashMap<>();

        String normalizedFormat = format;

        for (int i = 0; i < format.length(); i++) {
            char ch = format.charAt(i);
            boolean isLetter = (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');

            if (isLetter && !excludedChars.contains(ch)) {
                if (!skeleton.contains(ch + "")) {
                    excludedChars.add(ch);
                }

                if (!includedChars.containsKey(ch) && !excludedChars.contains(ch)) {
                    includedChars.put(ch, 0);
                    normalizedFormat = normalizedFormat.replaceAll(ch + "+", "" + ch);
                }
            }
        }

        Log.d("Format", "normalized format: " + normalizedFormat);

        if (!includedChars.isEmpty()) {
            for (int i = 0; i < skeleton.length(); i++) {
                char ch = skeleton.charAt(i);

                if (includedChars.containsKey(ch)) {
                    Integer counter = includedChars.get(ch);
                    counter++;
                    includedChars.put(ch, counter);
                }
            }


            for (Character ch : includedChars.keySet()) {
                final StringBuilder sb = new StringBuilder();
                final int n = includedChars.get(ch);

                for (int i = 0; i < n; i++) {
                    sb.append(ch);
                }

                Log.d("Format", "replacing '" + ch + "' with '" + sb.toString() + "' in " + normalizedFormat);

                normalizedFormat = normalizedFormat.replaceAll("" + ch, "" + sb.toString());
            }
        }

        format = normalizedFormat;

        if (!excludedChars.isEmpty()) {
            StringBuilder sb = new StringBuilder("[");
            Iterator<Character> it = excludedChars.iterator();

            while (it.hasNext()) {
                sb.append(it.next());

                if (it.hasNext()) {
                    sb.append('|');
                }
            }

            sb.append("][^\\w]*\\s*");

            Log.d("Format", "replace pattern: " + sb.toString() + ", current format: '" + format + "'");

            format = format.replaceAll(sb.toString(), "");
        }

        // - remove letters from format not in skeleton
        // - remove leading non-skeleton chars
        // - use the count of letters in skeleton to inflate the pattern
        // example: skeleton="yyyyMMMdd" + format="EEEE, MMMM d, y" -> "MMM dd, yyyy

        // [d][^\w]*\s*
        // [d][^\w]?\s*
        return format;
    }
}
