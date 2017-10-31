package com.app.camerademo;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AudioActivity extends AppCompatActivity {
    private MediaPlayer mPlayer;

    private ToggleButton tbtnRecordAndStop;
    private ImageButton play, deleteAudio;
    private ProgressBar progressBar;
    private TextView cancel, attachAudio, tvSeconsPlus, tvSeconsLess;
    private CountDownTimer mCountDownTimer;
    private MediaRecorder mRecorder = null;

    private String fileName = "";
    private boolean isReproduction = true, isRunning = false, isActive = true;
    private double countProgressPlusInit = 0, countProgressLessInit = 30;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        loadViews();
        loadListenerToTheControl();
        this.fileName = createPathForAudio();

        setPermissions();
    }


    private String createPathForAudio() {
        String timeStamp = new SimpleDateFormat(Constants.FORMAT_DATE_FILE).format(new Date());
        String storageDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        return storageDir + "/" + Constants.PREFIX_FILE_AUDIO + timeStamp + Constants.SUFFIX_FILE_AUDIO;
    }

    private void setPermissions() {
        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        Permissions.verifyPermissions(this, permissions);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.isActive = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            this.isRunning = false;
            this.mCountDownTimer.cancel();
            this.mRecorder.release();
            this.mRecorder = null;
            this.play.setBackgroundResource(R.drawable.ic_play_circle_filled_black_24dp);
            this.play.setEnabled(true);
            this.deleteAudio.setBackgroundResource(R.drawable.ic_close_black_24dp);
            this.deleteAudio.setEnabled(true);
            this.tbtnRecordAndStop.setBackgroundResource(R.drawable.ic_stop_black_24dp);
            this.tbtnRecordAndStop.setEnabled(false);
        }

        this.deleteAudio.setBackgroundResource(R.drawable.ic_close_black_24dp);

        if (this.mPlayer != null) {
            this.mCountDownTimer.cancel();
            pausedReproducction();
            this.play.setBackgroundResource(R.drawable.ic_play_circle_filled_black_24dp);
            this.play.setEnabled(true);
            this.deleteAudio.setBackgroundResource(R.drawable.ic_close_black_24dp);
            this.deleteAudio.setEnabled(true);
            this.tbtnRecordAndStop.setBackgroundResource(R.drawable.ic_stop_black_24dp);
            this.tbtnRecordAndStop.setEnabled(false);
        }
    }

    private void loadViews() {
        ImageView audioAnimation = (ImageView) findViewById(R.id.img_audio_animation);
        this.tbtnRecordAndStop = (ToggleButton) findViewById(R.id.tbtn_record_stop);
        this.play = (ImageButton) findViewById(R.id.btn_play);
        this.deleteAudio = (ImageButton) findViewById(R.id.btn_delete);
        this.progressBar = (ProgressBar) findViewById(R.id.progress_bar_recorder_audio);
        this.cancel = (TextView) findViewById(R.id.tv_cancel);
        this.attachAudio = (TextView) findViewById(R.id.tv_attach_audio);
        this.tvSeconsPlus = (TextView) findViewById(R.id.tv_plus_secons);
        this.tvSeconsLess = (TextView) findViewById(R.id.tv_less_secons);
    }

    private void loadListenerToTheControl() {
        this.tbtnRecordAndStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isRecord) {
                if ((Permissions.isGrantedPermissions(AudioActivity.this, Manifest.permission.RECORD_AUDIO) &&
                        Permissions.isGrantedPermissions(AudioActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                    managerRecord(isRecord);
                } else {
                    setPermissions();
                }
            }
        });

        this.play.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlay(isReproduction);
            }
        });

        this.deleteAudio.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDelete();
            }
        });

        this.cancel.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    private void managerRecord(boolean isRecord) {
        if (isRecord) {
            tbtnRecordAndStop.setBackgroundResource(R.drawable.ic_stop_black_24dp);
            play.setBackgroundResource(R.drawable.ic_play_circle_filled_black_24dp);
            play.setEnabled(false);
            deleteAudio.setBackgroundResource(R.drawable.ic_close_black_24dp);
            deleteAudio.setEnabled(false);
            attachAudio.setVisibility(View.GONE);
            clearComponents();
            onRecord(true);
        } else {
            attachAudio.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.VISIBLE);
            play.setEnabled(true);
            deleteAudio.setEnabled(true);
            isRunning = false;
            onRecord(false);
        }
    }

    private void onRecord(boolean start) {
        this.play.setVisibility(View.VISIBLE);
        this.deleteAudio.setVisibility(View.VISIBLE);
        Log.e(AudioActivity.class.getName(), "aqui estoy " + countProgressPlusInit);
        if (start) {
            Log.e(AudioActivity.class.getName(), "entro al start");
            startProgress();
            startRecording();
            this.tbtnRecordAndStop.setEnabled(false);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tbtnRecordAndStop.setEnabled(true);
                }
            }, 500);

        } else {
            Log.e(AudioActivity.class.getName(), "entro al stop");

            stopRecording();
        }
    }

    private void stopRecording() {
        Log.e(AudioActivity.class.getName(), "entro al stoprecording");
        //cancelo el conteo
        this.isRunning = false;
        this.play.setBackgroundResource(R.drawable.ic_play_circle_filled_black_24dp);
        this.deleteAudio.setBackgroundResource(R.drawable.ic_close_black_24dp);
        this.tbtnRecordAndStop.setBackgroundResource(R.drawable.ic_stop_black_24dp);
        this.tbtnRecordAndStop.setEnabled(false);

        this.mCountDownTimer.cancel();
        Log.e(AudioActivity.class.getName(), "entro al intermedio stoprecording");
        if (this.mRecorder != null) {
            Log.e(AudioActivity.class.getName(), "entro al stoprecording antes del stop del if");
            Log.e(AudioActivity.class.getName(), "dio " + (mRecorder != null));
            this.mRecorder.stop();
            Log.e(AudioActivity.class.getName(), "entro al stoprecording despues del stop");
            this.mRecorder.release();
            this.mRecorder = null;
            countProgressLessInit = 0;

            this.tvSeconsLess.setText(String.valueOf(Math.round(this.countProgressPlusInit)) + "s");
            this.tvSeconsPlus.setText("0s");
        }
    }

    private void startProgress() {
        Log.e(AudioActivity.class.getName(), "entro al sstartprogress");
        this.mCountDownTimer = new CountDownTimer(Constants.MAX_DURATION, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressBar.setProgress(progressBar.getProgress() + 100);
                double count = progressBar.getProgress() / 1000.0;
                countProgressPlusInit = +count;
                countProgressLessInit = 30 - count;
                changeTextView();
            }

            @Override
            public void onFinish() {
                isRunning = false;
                if (progressBar.getProgress() > 0) {
                    //termino de llenar el progress
                    progressBar.setProgress(progressBar.getProgress() + 1000);
                    play.setBackgroundResource(R.drawable.ic_play_circle_filled_black_24dp);
                    deleteAudio.setBackgroundResource(R.drawable.ic_close_black_24dp);
                    tbtnRecordAndStop.setBackgroundResource(R.drawable.ic_stop_black_24dp);
                    tbtnRecordAndStop.setChecked(false);
                }
            }
        };

        mCountDownTimer.start();
    }

    private void startRecording() {
        Log.e(AudioActivity.class.getName(), "entro al startrecording");

        this.isRunning = true;
        this.mRecorder = new MediaRecorder();
        this.mRecorder.setMaxDuration(Constants.MAX_DURATION);
        //Define la fuente de audio.
        this.mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //Define el formato de salida.
        this.mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //Define la codificación de vídeo.
        this.mRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        //Establece la ruta del archivo de salida para ser producido.
        this.mRecorder.setOutputFile(fileName);

        try {
            this.mRecorder.prepare();
            this.mRecorder.start();
            this.cancel.setVisibility(View.GONE);

        } catch (IOException e) {
            Log.e(AudioActivity.class.getName(), "prepare() failed");
        }
    }

    private void onDelete() {
       if (this.mPlayer != null) {
           this.mPlayer.stop();
           this.mPlayer.release();
       }
        this.isRunning = false;
        this.mCountDownTimer.cancel();
        clearComponents();
        this.attachAudio.setVisibility(View.GONE);
        this.play.setVisibility(View.GONE);
        this.deleteAudio.setVisibility(View.GONE);
        this.tbtnRecordAndStop.setChecked(false);
        this.tbtnRecordAndStop.setBackgroundResource(R.drawable.ic_mic_black_24dp);
        this.tbtnRecordAndStop.setEnabled(true);

        this.countProgressPlusInit = 0;
        this.countProgressLessInit = 30;

        changeTextView();
    }

    private void clearComponents() {
        if (this.countProgressLessInit != 30) {
            this.countProgressLessInit = 30;
            String countSecons = String.valueOf(countProgressPlusInit) + "s";
            this.tvSeconsPlus.setText(countSecons);
        }

        this.tvSeconsPlus.setText("");
        this.progressBar.setMax(30000);
        this.progressBar.setProgress(0);
        this.mPlayer = null;
        this.mRecorder = null;
    }




    private void onPlay(boolean isReproduction) {
        play.setBackgroundResource(isReproduction ? R.drawable.ic_pause_circle_filled_black_24dp : R.drawable.ic_play_circle_filled_black_24dp);
        deleteAudio.setBackgroundResource(R.drawable.ic_close_black_24dp);
        if (isReproduction) {
            if (mPlayer == null) {
                startPlaying();
            } else {
                continueReproduction();
            }

        } else {
            pausedReproducction();
        }
        this.isReproduction = !isReproduction;
    }

    private void pausedReproducction() {
        if (this.mPlayer != null) {
            this.isRunning = false;
            this.mPlayer.pause();
            this.mCountDownTimer.cancel();
        }
    }

    private void continueReproduction() {
        this.isRunning = true;
        this.mPlayer.seekTo((int) (countProgressPlusInit * 1000) - 100);
        this.mPlayer.start();
        startProgressPlay();
    }

    private void startPlaying() {
        this.isRunning = true;
        this.progressBar.setProgress(0);
        this.progressBar.setMax((int) (this.countProgressPlusInit * 1000) - 100);
        this.mPlayer = new MediaPlayer();
        try {
            this.mPlayer.setDataSource(this.fileName);
            this.mPlayer.prepare();
            this.mPlayer.start();
            this.progressBar.setProgress(0);
            this.countProgressLessInit = this.countProgressPlusInit;
            this.countProgressPlusInit = 0;
            startProgressPlay();
        } catch (IOException e) {
            Log.e(AudioActivity.class.getName(), "prepare() failed");
        }

        //listener para detectar el final del audio
        this.mPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        isReproduction = true;
                        play.setBackgroundResource(R.drawable.ic_play_circle_filled_black_24dp);
                        mPlayer = null;
                    }
                });
    }

    private void startProgressPlay() {
        this.mCountDownTimer = new CountDownTimer((long) ((this.countProgressLessInit - this.countProgressPlusInit) * 1000), 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                progressBar.setProgress(progressBar.getProgress() + 100);
                countProgressPlusInit += 0.1;
            }

            @Override
            public void onFinish() {
                isRunning = false;
                if (progressBar.getProgress() > 0) {
                    //termino de llenar el progress
                    countProgressPlusInit += 0.1;
                    play.setBackgroundResource(R.drawable.ic_play_circle_filled_black_24dp);
                    deleteAudio.setBackgroundResource(R.drawable.ic_close_black_24dp);
                    tbtnRecordAndStop.setBackgroundResource(R.drawable.ic_stop_black_24dp);
                    tbtnRecordAndStop.setChecked(false);
                }
            }
        };

        mCountDownTimer.start();
    }

    private void changeTextView() {
        String countSeconsPlus = String.valueOf(Math.round(this.countProgressPlusInit) + "s");
        String countSeconsLess = String.valueOf(Math.round(this.countProgressLessInit) + "s");

        this.tvSeconsPlus.setText(countSeconsPlus);
        this.tvSeconsLess.setText(countSeconsLess);
    }
}