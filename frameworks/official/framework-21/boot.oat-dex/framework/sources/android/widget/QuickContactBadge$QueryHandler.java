package android.widget;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telecom.PhoneAccount;

class QuickContactBadge$QueryHandler extends AsyncQueryHandler {
    final /* synthetic */ QuickContactBadge this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public QuickContactBadge$QueryHandler(QuickContactBadge quickContactBadge, ContentResolver cr) {
        super(cr);
        this.this$0 = quickContactBadge;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* access modifiers changed from: protected */
    @Override // android.content.AsyncQueryHandler
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        Uri lookupUri = null;
        Uri createUri = null;
        boolean trigger = false;
        Bundle extras = cookie != null ? (Bundle) cookie : new Bundle();
        switch (token) {
            case 0:
                if (cursor != null && cursor.moveToFirst()) {
                    lookupUri = ContactsContract.Contacts.getLookupUri(cursor.getLong(0), cursor.getString(1));
                    break;
                }
            case 1:
                if (cursor != null && cursor.moveToFirst()) {
                    lookupUri = ContactsContract.Contacts.getLookupUri(cursor.getLong(0), cursor.getString(1));
                    break;
                }
            case 2:
                trigger = true;
                createUri = Uri.fromParts("mailto", extras.getString("uri_content"), null);
                lookupUri = ContactsContract.Contacts.getLookupUri(cursor.getLong(0), cursor.getString(1));
                break;
            case 3:
                trigger = true;
                try {
                    createUri = Uri.fromParts(PhoneAccount.SCHEME_TEL, extras.getString("uri_content"), null);
                    lookupUri = ContactsContract.Contacts.getLookupUri(cursor.getLong(0), cursor.getString(1));
                    break;
                } catch (Throwable th) {
                    if (cursor != null) {
                        cursor.close();
                    }
                    throw th;
                }
        }
        if (cursor != null) {
            cursor.close();
        }
        QuickContactBadge.access$002(this.this$0, lookupUri);
        QuickContactBadge.access$100(this.this$0);
        if (trigger && lookupUri != null) {
            ContactsContract.QuickContact.showQuickContact(this.this$0.getContext(), this.this$0, lookupUri, 3, this.this$0.mExcludeMimes);
        } else if (createUri != null) {
            Intent intent = new Intent("com.android.contacts.action.SHOW_OR_CREATE_CONTACT", createUri);
            if (extras != null) {
                extras.remove("uri_content");
                intent.putExtras(extras);
            }
            this.this$0.getContext().startActivity(intent);
        }
    }
}
