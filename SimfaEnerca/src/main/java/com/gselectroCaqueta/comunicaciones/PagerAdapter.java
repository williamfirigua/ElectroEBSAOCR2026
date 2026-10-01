package com.gselectroCaqueta.comunicaciones;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                BackupFotosFragment tab1 = new BackupFotosFragment();
                return tab1;
            case 1:
                ConfigurarWEBFragment tab2 = new ConfigurarWEBFragment();
                return tab2;
            case 2:
                SeleccionRutaFragment tab3 = new SeleccionRutaFragment();
                return tab3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}