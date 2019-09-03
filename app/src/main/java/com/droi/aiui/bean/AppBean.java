package com.droi.aiui.bean;

import java.util.List;

/**
 * Created by cuixiaojun on 18-1-10.
 */

public class AppBean extends BaseBean {

    /**
     * category : CAPPU.applacition
     * intentType : custom
     * query : 打开通讯录
     * query_ws : 打开/VI//  通讯录/NN//
     * nlis : true
     * vendor : CAPPU
     * version : 3.0
     * semantic : [{"entrypoint":"ent","intent":"default_intent","score":1,"slots":[{"begin":2,"end":5,"name":"applacition","normValue":"通讯录","value":"通讯录"}]}]
     * state : null
     */

    private String category;
    private String intentType;
    private String query;
    private String query_ws;
    private String nlis;
    private String vendor;
    private String version;
    private Object state;
    private List<SemanticBean> semantic;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIntentType() {
        return intentType;
    }

    public void setIntentType(String intentType) {
        this.intentType = intentType;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getQuery_ws() {
        return query_ws;
    }

    public void setQuery_ws(String query_ws) {
        this.query_ws = query_ws;
    }

    public String getNlis() {
        return nlis;
    }

    public void setNlis(String nlis) {
        this.nlis = nlis;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Object getState() {
        return state;
    }

    public void setState(Object state) {
        this.state = state;
    }

    public List<SemanticBean> getSemantic() {
        return semantic;
    }

    public void setSemantic(List<SemanticBean> semantic) {
        this.semantic = semantic;
    }

    public static class SemanticBean {
        /**
         * entrypoint : ent
         * intent : default_intent
         * score : 1
         * slots : [{"begin":2,"end":5,"name":"applacition","normValue":"通讯录","value":"通讯录"}]
         */

        private String entrypoint;
        private String intent;
        private int score;
        private List<SlotsBean> slots;

        public String getEntrypoint() {
            return entrypoint;
        }

        public void setEntrypoint(String entrypoint) {
            this.entrypoint = entrypoint;
        }

        public String getIntent() {
            return intent;
        }

        public void setIntent(String intent) {
            this.intent = intent;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public List<SlotsBean> getSlots() {
            return slots;
        }

        public void setSlots(List<SlotsBean> slots) {
            this.slots = slots;
        }

        public static class SlotsBean {
            /**
             * begin : 2
             * end : 5
             * name : applacition
             * normValue : 通讯录
             * value : 通讯录
             */

            private int begin;
            private int end;
            private String name;
            private String normValue;
            private String value;

            public int getBegin() {
                return begin;
            }

            public void setBegin(int begin) {
                this.begin = begin;
            }

            public int getEnd() {
                return end;
            }

            public void setEnd(int end) {
                this.end = end;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getNormValue() {
                return normValue;
            }

            public void setNormValue(String normValue) {
                this.normValue = normValue;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }
}