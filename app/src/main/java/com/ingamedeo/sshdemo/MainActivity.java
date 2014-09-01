package com.ingamedeo.sshdemo;

import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends ActionBarActivity {

    TextView outputView;
    EditText hostView;
    EditText usernameView;
    EditText passwordView;
    EditText commandView;

    Button buttonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        outputView = (TextView) findViewById(R.id.output);
        hostView = (EditText) findViewById(R.id.host);
        usernameView = (EditText) findViewById(R.id.username);
        passwordView = (EditText) findViewById(R.id.password);
        passwordView.setTypeface(Typeface.DEFAULT);
        commandView = (EditText) findViewById(R.id.command);

        buttonView = (Button) findViewById(R.id.buttonexecute);

        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String host = hostView.getText().toString();
                String username = usernameView.getText().toString();
                String password = passwordView.getText().toString();
                String command = commandView.getText().toString();

                buttonView.setClickable(false);
                buttonView.setEnabled(false);

                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(buttonView.getWindowToken(), 0);

                new ExecTask().execute(host, username, password, command);
            }
        });
    }

    private class ExecTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            outputView.setText(getResources().getString(R.string.connecting));
        }

        @Override
        protected String doInBackground(String... param) {

            String host = param[0];
            String username = param[1];
            String password = param[2];
            String command = param[3];

            StringBuilder sb = null;

            try {

            /* Init new JSch Object */
                JSch mSsh = new JSch();

			/* Init Session */
                Session mSession = mSsh.getSession(username, host, 22);

                mSession.setPassword(password);

			/* Don't request the user to verify any SSH Key */
                mSession.setConfig("StrictHostKeyChecking", "no");

			/* Establish Session */
                mSession.connect();

			/* Open Execution Channel */
                ChannelExec mChannel = (ChannelExec) mSession.openChannel("exec");
            /* Set my command */
                mChannel.setCommand(command);

			/* Get Input Stream */
                InputStream in = mChannel.getInputStream();

			/* Execute our command */
                mChannel.connect();

			/* Mmm...we get an InputStream... we can get try to read it */
                InputStreamReader reader = new InputStreamReader(in, "UTF-8");
                BufferedReader buffReader = new BufferedReader(reader);
                sb = new StringBuilder();
                String s;
                while ((s = buffReader.readLine()) != null) {
                    sb.append(s);
                }

            } catch (JSchException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (sb!=null) {
                return sb.toString();
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            buttonView.setEnabled(true);
            buttonView.setClickable(true);

            if (response!=null) {
                Log.i("log_tag", "Response: " + response);
                outputView.setText(response);
            }
        }
    }
}
