package com.offsec.nethunter;

import android.util.Log;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


public class ShellExecuter {

    final static String TAG = "ShellExecutor";

    public ShellExecuter() {

    }

    public String Executer(String command) {
        StringBuilder output = new StringBuilder();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public String Executer(String command[]) {
        StringBuilder output = new StringBuilder();
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public boolean RunAsRoot(String[] command) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            for (String tmpmd : command) {
                os.writeBytes(tmpmd + '\n');
            }
            os.writeBytes("exit\n");
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String RunAsRootWithException(String command) throws RuntimeException {
        try {
            String output = "";
            String line;
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            stdin.write((command + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                output = output + line;
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                Log.e("Shell Error:", line);
                throw new RuntimeException();
            }
            br.close();
            process.waitFor();
            process.destroy();
            return output;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean isRootAvailable() {
        String result = RunAsRootOutput("busybox id -u");
        return result.matches("0");
    }

    public String RunAsRootOutput(String command) {
        String output = "";
        String line;
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            stdin.write((command + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                output = output + line;
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                Log.e("Shell Error:", line);
            }
            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException e) {
            Log.d(TAG, "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d(TAG, "An InterruptedException was caught: " + ex.getMessage());
        }
        return output;
    }

    // this method accepts a text viu (prefect for cases like mana fragment)
    // if you need to manipulate the outpput use the SYNC method. (down)
    public void ReadFile_ASYNC(String _path, final EditText v) {
        final String command = "cat " + _path;
        new Thread(new Runnable() {
            public void run() {
                String output = "";
                try {
                    Process  p = Runtime.getRuntime().exec("su -c " + command);
                    p.waitFor();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output = output +  line + "\n";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                final String _output = output;
                v.post(new Runnable() {
                    @Override
                    public void run() {
                     v.setText(_output.toString());
                    }
                });
            }
        }).start();
    }

    // WRAP THIS IN THE BACKGROUND IF POSIBLE WHE USING IT
    public String ReadFile_SYNC(String _path) {

        StringBuilder output = new StringBuilder();
        String command = "cat " + _path;
        Process p;
        try {
            p = Runtime.getRuntime().exec("su -c " + command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }

    public boolean SaveFileContents(String contents, String _path){
        Boolean isSaved = RunAsRoot(new String[]{"echo '" + contents + "' > " + _path});
        return  isSaved;
    }
}
