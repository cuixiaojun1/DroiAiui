package com.droi.aiui.bean;

import java.util.List;

/**
 * Created by cuixiaojun on 18-2-2.
 */

public class MusicBean extends BaseBean {

    /**
     * category : CAPPU.music_demo
     * intentType : custom
     * vendor : CAPPU
     * version : 4.0
     * semantic : [{"entrypoint":"ent","intent":"search_by_artist","score":0.906556248664856,"slots":[{"begin":3,"end":6,"name":"singer","normValue":"刘德华","value":"刘德华"},{"begin":7,"end":9,"name":"song","normValue":"冰雨","value":"冰雨"}],"template":"来一首{singer}的{song}"}]
     * state : null
     */

    private String category;
    private String intentType;
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
         * intent : search_by_artist
         * score : 0.906556248664856
         * slots : [{"begin":3,"end":6,"name":"singer","normValue":"刘德华","value":"刘德华"},{"begin":7,"end":9,"name":"song","normValue":"冰雨","value":"冰雨"}]
         * template : 来一首{singer}的{song}
         */

        private String entrypoint;
        private String intent;
        private double score;
        private String template;
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

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public String getTemplate() {
            return template;
        }

        public void setTemplate(String template) {
            this.template = template;
        }

        public List<SlotsBean> getSlots() {
            return slots;
        }

        public void setSlots(List<SlotsBean> slots) {
            this.slots = slots;
        }

        public static class SlotsBean {
            /**
             * begin : 3
             * end : 6
             * name : singer
             * normValue : 刘德华
             * value : 刘德华
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