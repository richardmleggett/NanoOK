package nanook;

public class FileWatcherItem {
    private String pathname;
    private int passOrFail;
    
    public FileWatcherItem(String p, int pf) {
        pathname = p;
        passOrFail = pf;
    }
    
    public String getPathname() {
        return pathname;
    }
    
    public int getPassOrFail() {
        return passOrFail;
    }
    
    public boolean isPass() {
        return passOrFail == NanoOKOptions.READTYPE_PASS ? true: false;
    }
    
    public boolean isFail() {
        return passOrFail == NanoOKOptions.READTYPE_FAIL ? true: false;
    }

    public boolean isCombined() {
        return passOrFail == NanoOKOptions.READTYPE_COMBINED ? true: false;
    }
}
