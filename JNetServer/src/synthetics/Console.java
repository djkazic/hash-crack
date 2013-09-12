package synthetics;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import javax.swing.JTextArea;

public class Console extends OutputStream {
	public static boolean doneBefore = false;
    ArrayList<String> data = new ArrayList<String>();

    private JTextArea output;

    public Console(JTextArea output) {
        this.output = output;
    }

    private void fireDataWritten(boolean overwrite) {
        int lines = data.size();
        StringBuilder bldr = new StringBuilder();
        int maxLines = 7;
        int i = 0;
        if (lines > maxLines) { 
        	i = lines - maxLines;
        }
        ArrayList<String> data2 = new ArrayList<String>();
        for (;i < lines; i ++) {
        	bldr.append(data.get(i));
            data2.add(data.get(i));
        }
        data = data2;
        output.setText(bldr.toString());
    }

    @Override
    public void write(int i) throws IOException {
    	//LOLNO
    }
    
    public void swrite(String string) {
    	String processString = string + "\n";
    	data.add(processString);
    	fireDataWritten(false);
    }
    
    public void sfwrite(String string) {
    	data.add(string + '\n');
      	fireDataWritten(true);
    }

}