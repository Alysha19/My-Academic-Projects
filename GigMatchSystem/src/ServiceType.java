public class ServiceType {

    public static final int PAINT = 0;
    public static final int WEB_DEV = 1;
    public static final int GRAPHIC_DESIGN = 2;
    public static final int DATA_ENTRY = 3;
    public static final int TUTORING = 4;
    public static final int CLEANING = 5;
    public static final int WRITING = 6;
    public static final int PHOTOGRAPHY = 7;
    public static final int PLUMBING = 8;
    public static final int ELECTRICAL = 9;

    public static final int SERVICE_COUNT = 10;

    public static int fromString(String s) {
        switch (s) {
            case "paint":            return PAINT;
            case "web_dev":          return WEB_DEV;
            case "graphic_design":   return GRAPHIC_DESIGN;
            case "data_entry":       return DATA_ENTRY;
            case "tutoring":         return TUTORING;
            case "cleaning":         return CLEANING;
            case "writing":          return WRITING;
            case "photography":      return PHOTOGRAPHY;
            case "plumbing":         return PLUMBING;
            case "electrical":       return ELECTRICAL;
            default: return -1; // handle invalid service name
        }
    }

    public static String toString(int service) {
        switch (service) {
            case PAINT:          return "paint";
            case WEB_DEV:        return "web_dev";
            case GRAPHIC_DESIGN: return "graphic_design";
            case DATA_ENTRY:     return "data_entry";
            case TUTORING:       return "tutoring";
            case CLEANING:       return "cleaning";
            case WRITING:        return "writing";
            case PHOTOGRAPHY:    return "photography";
            case PLUMBING:       return "plumbing";
            case ELECTRICAL:     return "electrical";
            default: return "unknown";
        }
    }
}
