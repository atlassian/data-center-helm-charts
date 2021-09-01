package test.model;

/**
 * Helpers to deal with Synchrony in tests
 */
public class Synchrony {

    public static String synchronyStatefulSetName() {
        return Product.confluence.getHelmReleaseName() + "-synchrony";
    }

}
