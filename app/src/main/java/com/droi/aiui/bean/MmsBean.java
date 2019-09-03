package com.droi.aiui.bean;

import java.util.List;

/**
 * Created by hejianfeng on 2018/01/10.
 */

public class MmsBean extends BaseBean {

    /**
     * category : CAPPU.cappu_mms
     * intentType : custom
     * query : 发短信给张三
     * query_ws : 发短信/VI// 给/UH// 张三/NPP//
     * nlis : true
     * vendor : CAPPU
     * version : 4.0
     * semantic : [{"entrypoint":"ent","intent":"default_intent","score":1,"slots":[{"begin":4,"end":6,"name":"contact","normValue":"张三","value":"张三"}]}]
     * state : null
     */

    private List<SemanticBean> semantic;

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
         * slots : [{"begin":4,"end":6,"name":"contact","normValue":"张三","value":"张三"}]
         */

        private String intent;
        private List<SlotsBean> slots;

        public String getIntent() {
            return intent;
        }

        public void setIntent(String intent) {
            this.intent = intent;
        }

        public List<SlotsBean> getSlots() {
            return slots;
        }

        public void setSlots(List<SlotsBean> slots) {
            this.slots = slots;
        }

        public static class SlotsBean {
            /**
             * begin : 4
             * end : 6
             * name : contact
             * normValue : 张三
             * value : 张三
             */

            private String name;
            private String normValue;
            private String value;

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