package settings;

/**
 * This enumerable type lists the various application-specific property types listed in the initial set of properties to
 * be loaded from the workspace properties <code>xml</code> file specified by the initialization parameters.
 *
 * @author Ritwik Banerjee
 * @see vilij.settings.InitializationParams
 */
public enum AppPropertyTypes {

    /* resource files and folders */
    DATA_RESOURCE_PATH,

    /* user interface icon file names */
    SCREENSHOT_ICON,

    /* tooltips for user interface buttons */
    SCREENSHOT_TOOLTIP,

    /* error messages */
    RESOURCE_SUBDIR_NOT_FOUND,

    /* application-specific message titles */
    SAVE_UNSAVED_WORK_TITLE,

    /* application-specific messages */
    SAVE_UNSAVED_WORK,

    /* application-specific parameters */
    DATA_FILE_EXT,
    DATA_FILE_EXT_DESC,
    TEXT_AREA,
    SPECIFIED_FILE,
    CHART_TITLE,
    DISPLAY_TEXT,
    LEFT_VBOX_TITLE,
    LEFT_VBOX_TITLE_FONT,
    LEFT_VBOX_TITLE_SIZE,
    ERROR_LINE,
    DUPLICATE_NAME,
    CONFIRMATION,
    CONFIRMATION_MSG_PART1,
    CONFIRMATION_MSG_PART2,
    SAVE_IMAGE_TITLE,
    SAVE_FILE_EXT_DESC,
    SAVE_IMAGE_FILE_EXT,




}
