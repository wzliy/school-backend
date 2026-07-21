package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.page.model.PageSection;
import com.zlwang.school.modules.page.model.PageSectionType;
import java.util.List;
import java.util.Map;

public record PortalPageSectionResponse(
    String sectionCode,
    String sectionName,
    PageSectionType sectionType,
    PortalColumnResponse sourceColumn,
    Integer displayCount,
    String displayStyle,
    Map<String, Object> config,
    int sortNo,
    List<PortalBannerResponse> banners,
    List<PortalContentSummaryResponse> contents,
    List<PortalColumnResponse> links,
    List<PortalFriendLinkResponse> friendLinks,
    Map<String, String> contact
) {

    public PortalPageSectionResponse {
        config = config == null ? Map.of() : Map.copyOf(config);
        banners = banners == null ? List.of() : List.copyOf(banners);
        contents = contents == null ? List.of() : List.copyOf(contents);
        links = links == null ? List.of() : List.copyOf(links);
        friendLinks = friendLinks == null ? List.of() : List.copyOf(friendLinks);
        contact = contact == null ? Map.of() : Map.copyOf(contact);
    }

    public static Builder from(PageSection section) {
        return new Builder(section);
    }

    public static final class Builder {

        private final PageSection section;
        private PortalColumnResponse sourceColumn;
        private List<PortalBannerResponse> banners = List.of();
        private List<PortalContentSummaryResponse> contents = List.of();
        private List<PortalColumnResponse> links = List.of();
        private List<PortalFriendLinkResponse> friendLinks = List.of();
        private Map<String, String> contact = Map.of();

        private Builder(PageSection section) {
            this.section = section;
        }

        public Builder sourceColumn(PortalColumnResponse value) {
            sourceColumn = value;
            return this;
        }

        public Builder banners(List<PortalBannerResponse> value) {
            banners = value;
            return this;
        }

        public Builder contents(List<PortalContentSummaryResponse> value) {
            contents = value;
            return this;
        }

        public Builder links(List<PortalColumnResponse> value) {
            links = value;
            return this;
        }

        public Builder friendLinks(List<PortalFriendLinkResponse> value) {
            friendLinks = value;
            return this;
        }

        public Builder contact(Map<String, String> value) {
            contact = value;
            return this;
        }

        public PortalPageSectionResponse build() {
            return new PortalPageSectionResponse(
                section.sectionCode(),
                section.sectionName(),
                section.sectionType(),
                sourceColumn,
                section.displayCount(),
                section.displayStyle(),
                section.config(),
                section.sortNo(),
                banners,
                contents,
                links,
                friendLinks,
                contact
            );
        }
    }
}
