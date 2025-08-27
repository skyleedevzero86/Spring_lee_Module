package com.sleekydz86.kmsage.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageTemplate {
    @JsonProperty("object_type")
    private String objectType;
    private Content content;
    private Social social;
    private Button[] buttons;

    public static class Content {
        private String title;
        private String description;
        @JsonProperty("image_url")
        private String imageUrl;
        @JsonProperty("image_width")
        private int imageWidth;
        @JsonProperty("image_height")
        private int imageHeight;
        private Link link;

        public Content() {}

        public Content(String title, String description, String imageUrl) {
            this.title = title;
            this.description = description;
            this.imageUrl = imageUrl;
            this.imageWidth = 640;
            this.imageHeight = 640;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public int getImageWidth() { return imageWidth; }
        public void setImageWidth(int imageWidth) { this.imageWidth = imageWidth; }

        public int getImageHeight() { return imageHeight; }
        public void setImageHeight(int imageHeight) { this.imageHeight = imageHeight; }

        public Link getLink() { return link; }
        public void setLink(Link link) { this.link = link; }
    }

    public static class Link {
        @JsonProperty("web_url")
        private String webUrl;
        @JsonProperty("mobile_web_url")
        private String mobileWebUrl;

        public Link() {}

        public Link(String webUrl, String mobileWebUrl) {
            this.webUrl = webUrl;
            this.mobileWebUrl = mobileWebUrl;
        }

        public String getWebUrl() { return webUrl; }
        public void setWebUrl(String webUrl) { this.webUrl = webUrl; }

        public String getMobileWebUrl() { return mobileWebUrl; }
        public void setMobileWebUrl(String mobileWebUrl) { this.mobileWebUrl = mobileWebUrl; }
    }

    public static class Social {
        @JsonProperty("like_count")
        private int likeCount;
        @JsonProperty("comment_count")
        private int commentCount;

        public Social() {}

        public Social(int likeCount, int commentCount) {
            this.likeCount = likeCount;
            this.commentCount = commentCount;
        }

        public int getLikeCount() { return likeCount; }
        public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

        public int getCommentCount() { return commentCount; }
        public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    }

    public static class Button {
        private String title;
        private Link link;

        public Button() {}

        public Button(String title, Link link) {
            this.title = title;
            this.link = link;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public Link getLink() { return link; }
        public void setLink(Link link) { this.link = link; }
    }

    public MessageTemplate() {}

    public MessageTemplate(String objectType, Content content) {
        this.objectType = objectType;
        this.content = content;
    }

    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }

    public Content getContent() { return content; }
    public void setContent(Content content) { this.content = content; }

    public Social getSocial() { return social; }
    public void setSocial(Social social) { this.social = social; }

    public Button[] getButtons() { return buttons; }
    public void setButtons(Button[] buttons) { this.buttons = buttons; }
}
