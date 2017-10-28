package com.app.camerademo;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by leidyzulu on 26/10/17.
 */

public class AttachImageAdapter  extends RecyclerView.Adapter<AttachImageAdapter.ViewHolder>  {


        private List<String> listFilesName;
        private final Context context;
        private int widthLayout, heightLayout;

        public AttachImageAdapter(Context context) {
            this.context = context;
        }

       // public void setCount();

        public void setFiles(ArrayList<String> arrayFiles) {
            this.listFilesName = arrayFiles;
        }

        public void setSize(int width, int height) {
            this.widthLayout = width;
            this.heightLayout = (int)(height * 0.74);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final ImageView deleteImage, attachImageAudio;
            final TextView attach_tamanio;

            public ViewHolder(View itemView) {
                super(itemView);
                deleteImage = (ImageView) itemView.findViewById(R.id.attach_delete);
                attachImageAudio = (ImageView) itemView.findViewById(R.id.attach_image_audio);
                attach_tamanio = (TextView) itemView.findViewById(R.id.attach_tamanio);
                deleteImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if (v.equals(deleteImage)) {
                    listFilesName.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    notifyItemRangeChanged(getAdapterPosition(), getItemCount());
                   // setCount();
                }
            }
        }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attach_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            String fileName = listFilesName.get(position);
            if (fileName.contains(Constants.PREFIX_FILE_AUDIO)) {
                Glide.with(context).load(R.drawable.ic_close_black_24dp)
                        .override(widthLayout, heightLayout)
                        .into(holder.attachImageAudio);
            } else {
                Glide.with(context).load(fileName)
                        .override(widthLayout, heightLayout)
                        .into(holder.attachImageAudio);
            }

            holder.deleteImage.setVisibility(View.VISIBLE);
            holder.attach_tamanio.setText(getSizeFile(fileName));
        }

        @Override
        public int getItemCount() {
            return listFilesName.size();
        }


    public String getSizeFile(String path) {
        File file = new File(path);
        float size = 0;
        DecimalFormat decimalFormat = new DecimalFormat(Constants.FORMAT_MB);

        if (file.length() == 0) {
            InputStream inputStream;
            try {
                inputStream = context.getContentResolver().openInputStream(Uri.parse(path));
                assert inputStream != null;
                size = (float) inputStream.available();
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } else {
            size = (float) file.length();
        }

        if (size >= Constants.FACTOR_CONVERSION_MB) {
            return decimalFormat.format(size / Constants.FACTOR_CONVERSION_MB) + Constants.MEGABYTE;
        } else {
            return decimalFormat.format(size / Constants.FACTOR_CONVERSION_KB) + Constants.KILOBYTE;
        }
    }
    }


