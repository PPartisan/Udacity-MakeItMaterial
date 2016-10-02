package com.example.xyzreader.util;

import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.style.ForegroundColorSpan;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

public final class TextUtils {

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String input) {

        Spanned result;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = Html.fromHtml(input, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(input);
        }

        return result;
    }

    public static CharSequence getByLineText(Resources res, Cursor cursor, boolean isColoured) {

        final long publishedDate = cursor.getLong(ArticleLoader.Query.PUBLISHED_DATE);

        final String relativeTimeSpanString = DateUtils.getRelativeTimeSpanString(
                publishedDate,
                System.currentTimeMillis(),
                DateUtils.HOUR_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL
        ).toString();

        final String author = cursor.getString(ArticleLoader.Query.AUTHOR);

        final String unformattedByLine = res.getString(R.string.by_line, relativeTimeSpanString, author);

        SpannableString formattedByLine = new SpannableString(unformattedByLine);

        if (isColoured) {

            formattedByLine.setSpan(
                    new ForegroundColorSpan(Color.WHITE),
                    (formattedByLine.length() - author.length()),
                    formattedByLine.length(),
                    SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        return formattedByLine;

    }

}
