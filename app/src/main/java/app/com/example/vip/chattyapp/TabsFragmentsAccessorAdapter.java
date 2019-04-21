package app.com.example.vip.chattyapp;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsFragmentsAccessorAdapter extends FragmentPagerAdapter {
    public TabsFragmentsAccessorAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;
            //case 1:
                //GroupFragment groupFragment = new GroupFragment();
                //return groupFragment;
            case 1:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;
            case 2:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3; //number of fragments
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Chats";
            //case 1:
                //return "Groups";
            case 1:
                return "Contacts";
            case 2:
                return "Requests";
            default:
                return null;
        }
    }
}
