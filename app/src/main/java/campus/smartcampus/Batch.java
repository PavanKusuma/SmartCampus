package campus.smartcampus;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class Batch extends Fragment implements Home.FragmentLifeCycle {

    RelativeLayout itemView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        itemView = (RelativeLayout) inflater.inflate(R.layout.batch, container, false);
        return itemView;
    }

    @Override
    public void onResumeFragment() {

    }
}
