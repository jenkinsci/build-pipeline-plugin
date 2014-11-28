package au.com.centrumsystems.hudson.plugin.buildpipeline;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Message resource bundle utility
 * 
 * @author Unknown
 */
public final class Strings {
    /**
     * bundle name
     */
    private static final String BUNDLE_NAME = "au.com.centrumsystems.hudson.plugin.buildpipeline.messages"; //$NON-NLS-1$

    /**
     * message resource bundle
     */
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * 
     */
    private Strings() {
    }

    /**
     * 
     * @param key
     *            key to resource bundle
     * @return resource of the key.
     */
    public static String getString(final String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (final MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
