package gitlet;

import javax.sound.midi.SysexMessage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class test {
    public static void main(String[] args) {
        File f = Utils.join(Repository.CWD, "test");
        f.mkdir();
        List<String> t = Utils.plainFilenamesIn(f);
        System.out.println(t.size());
    }
}
