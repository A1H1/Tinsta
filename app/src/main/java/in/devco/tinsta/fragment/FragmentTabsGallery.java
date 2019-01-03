package in.devco.tinsta.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.devco.tinsta.R;

public class FragmentTabsGallery extends Fragment {

    public static FragmentTabsGallery newInstance() {
        return new FragmentTabsGallery();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tabs_gallery, container, false);

        return root;
    }
}