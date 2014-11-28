package au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport;

public interface Condition {

    boolean isSatisfied() throws Exception;
    String describe();
}
