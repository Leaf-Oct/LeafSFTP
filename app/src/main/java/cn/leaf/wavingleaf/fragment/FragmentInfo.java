package cn.leaf.wavingleaf.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.leaf.wavingleaf.R;
import cn.leaf.wavingleaf.databinding.FragmentInfoBinding;


public class FragmentInfo extends DialogFragment {

    FragmentInfoBinding binding;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding=FragmentInfoBinding.inflate(getLayoutInflater());
        return new AlertDialog.Builder(getActivity()).setTitle("Leaf SFTP Server").setView(binding.getRoot()).create();
    }
}