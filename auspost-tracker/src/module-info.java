module auspost.tracker {
    requires java.sql;

    requires post.tracker.api;
    requires gson;
    requires jdk.incubator.httpclient;

    exports au.com.posttracker.services.spi.auspost;
}
