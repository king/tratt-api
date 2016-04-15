/*******************************************************************************
 * (C) king.com Ltd 2016
 *  
 *******************************************************************************/
/*
 * // (C) king.com Ltd 2014
 */

package com.king.tratt.tdl;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class TdlInternal {

    public String comment = "";
    /* Needs to be a LinkedHasSet to guarantee order AND no duplicates */
    public Set<String> variables = new LinkedHashSet<>();
    public List<SequenceInternal> sequences = new ArrayList<>();

    @Override
    public String toString() {
        return Tdl.asJsonPrettyPrinted(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result + ((sequences == null) ? 0 : sequences.hashCode());
        result = prime * result + ((variables == null) ? 0 : variables.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TdlInternal other = (TdlInternal) obj;
        if (comment == null) {
            if (other.comment != null) {
                return false;
            }
        } else if (!comment.equals(other.comment)) {
            return false;
        }
        if (sequences == null) {
            if (other.sequences != null) {
                return false;
            }
        } else if (!sequences.equals(other.sequences)) {
            return false;
        }
        if (variables == null) {
            if (other.variables != null) {
                return false;
            }
        } else if (!variables.equals(other.variables)) {
            return false;
        }
        return true;
    }

    static class SequenceInternal {

        public String type;
        public String name;
        public String sequenceMaxTime = "pt15m";
        public List<CheckPointInternal> checkPoints;

        @Override
        public String toString() {
            return Tdl.asJsonPrettyPrinted(this);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((checkPoints == null) ? 0 : checkPoints.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((sequenceMaxTime == null) ? 0 : sequenceMaxTime.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            SequenceInternal other = (SequenceInternal) obj;
            if (checkPoints == null) {
                if (other.checkPoints != null) {
                    return false;
                }
            } else if (!checkPoints.equals(other.checkPoints)) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (sequenceMaxTime == null) {
                if (other.sequenceMaxTime != null) {
                    return false;
                }
            } else if (!sequenceMaxTime.equals(other.sequenceMaxTime)) {
                return false;
            }
            if (type == null) {
                if (other.type != null) {
                    return false;
                }
            } else if (!type.equals(other.type)) {
                return false;
            }
            return true;
        }

    }

    static class CheckPointInternal {

        public String eventType;
        public String match = "";
        public List<String> set = new ArrayList<>();
        public String validate = "";

        @Override
        public String toString() {
            return Tdl.asJsonPrettyPrinted(this);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
            result = prime * result + ((match == null) ? 0 : match.hashCode());
            result = prime * result + ((set == null) ? 0 : set.hashCode());
            result = prime * result + ((validate == null) ? 0 : validate.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            CheckPointInternal other = (CheckPointInternal) obj;
            if (eventType == null) {
                if (other.eventType != null) {
                    return false;
                }
            } else if (!eventType.equals(other.eventType)) {
                return false;
            }
            if (match == null) {
                if (other.match != null) {
                    return false;
                }
            } else if (!match.equals(other.match)) {
                return false;
            }
            if (set == null) {
                if (other.set != null) {
                    return false;
                }
            } else if (!set.equals(other.set)) {
                return false;
            }
            if (validate == null) {
                if (other.validate != null) {
                    return false;
                }
            } else if (!validate.equals(other.validate)) {
                return false;
            }
            return true;
        }

    }
}
