/*******************************************************************************
 * (C) king.com Ltd 2016
 *
 *******************************************************************************/
package com.king.tratt;

import static com.king.tratt.Tratt.util;
import static com.king.tratt.Tratt.values;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.king.tratt.spi.Context;
import com.king.tratt.spi.Event;
import com.king.tratt.spi.Value;

class FunctionFactoryProvider {

    private Map<String, FunctionFactory> functions = new HashMap<>();

    FunctionFactoryProvider() {
        addFunction(jsonField());
        addFunction(substr());
        addFunction(concat());
        addFunction(split());

    }

    private void addFunction(FunctionFactory func) {
        functions.put(func.getName(), func);
    }

    FunctionFactory get(String name) {
        return functions.get(name);
    }

    List<String> getFunctionNames() {
        return new ArrayList<>(functions.keySet());
    }

    static FunctionFactory jsonField() {
        return new FunctionFactory() {
            Value pathValue;
            Value jsonValue;
            JsonParser jsonParser;

            @Override
            public String getName() {
                return "jsonfield";
            }

            @Override
            public int getNumberOfArguments() {
                return 2;
            }

            @Override
            public Value create(List<Value> arguments) {
                pathValue = arguments.get(0);
                jsonValue = arguments.get(1);
                jsonParser = new JsonParser();

                return new Value() {

                    @Override
                    public String toString() {
                        return String.format("jsonfield('%s', '%s')", pathValue, jsonValue);
                    }

                    @Override
                    public String toDebugString(Event e, Context context) {
                        return util.format(e, context, "[[source:jsonfield('~v', '~v')]]~q",
                                pathValue, jsonValue, this);
                    }

                    @Override
                    protected Object getImp(Event e, Context context) {
                        String path = pathValue.asString(e, context);
                        String json = jsonValue.asString(e, context);

                        String result = null;
                        try {
                            result = getJsonFieldValue(path, json);
                        } catch (Throwable t) {
                            result = "[@ERROR malformed json string]";
                        }
                        return values.parseSupportedType(result);
                    }

                    private String getJsonFieldValue(String path, String json) {
                        JsonElement jsonElem = jsonParser.parse(json);
                        for (String subPath : path.split("\\.")) {
                            JsonObject jsonObj = jsonElem.getAsJsonObject();
                            jsonElem = jsonObj.get(subPath);
                            if (jsonElem == null) {
                                return String.format("[@ERROR incorrect json path: '%s']", path);
                            }
                        }
                        if (jsonElem.isJsonObject()) {
                            return jsonElem.toString();
                        }
                        return jsonElem.getAsString();

                    }
                };
            }
        };
    }

    static FunctionFactory substr() {
        return new FunctionFactory() {

            Value fromValue;
            Value toValue;
            Value strValue;

            @Override
            public String getName() {
                return "substr";
            }

            @Override
            public int getNumberOfArguments() {
                return 3;
            }

            @Override
            public Value create(List<Value> args) {
                fromValue = args.get(0);
                toValue = args.get(1);
                strValue = args.get(2);

                return new Value() {

                    @Override
                    public String toString() {
                        return String.format("substr('%s', '%s', '%s')", fromValue, toValue,
                                strValue);
                    }

                    @Override
                    public String toDebugString(Event e, Context context) {
                        return util.format(e, context, "[[source:substr(~v, ~v, '~v')]]'~v'",
                                fromValue, toValue, strValue, this);
                    }

                    @Override
                    protected String getImp(Event e, Context context) {
                        int from = ((Long) fromValue.get(e, context)).intValue();
                        int to = ((Long) toValue.get(e, context)).intValue();
                        String str = (String) strValue.get(e, context);
                        return str.substring(from, to);
                    }
                };
            }
        };
    }

    static FunctionFactory split() {
        return new FunctionFactory() {

            private Value strValue;
            private Value delimiterValue;
            private Value indexValue;

            @Override
            public String getName() {
                return "split";
            }

            @Override
            public int getNumberOfArguments() {
                return 3;
            }

            @Override
            public Value create(List<Value> arguments) {
                strValue = arguments.get(0);
                delimiterValue = arguments.get(1);
                indexValue = arguments.get(2);
                return new Value() {

                    @Override
                    public String toString() {
                        return String.format("split('%s', '%s', '%s')", strValue, delimiterValue,
                                indexValue);
                    }

                    @Override
                    public String toDebugString(Event e, Context context) {
                        return util.format(e, context, "[[source:split('~v', '~v', ~v)]]'~v'",
                                strValue, delimiterValue, indexValue, this);
                    }

                    @Override
                    protected String getImp(Event e, Context context) {
                        String str = strValue.asString(e, context);
                        String delimiter = delimiterValue.asString(e, context);
                        String[] strs = str.split(delimiter);
                        int i = ((Long) indexValue.get(e, context)).intValue();
                        if (i < strs.length) {
                            return strs[i];
                        } else {
                            return String.format("[@ERROR array index '%s' out of bounce]", i);
                        }
                    }
                };
            }
        };
    }

    static FunctionFactory concat() {
        return new FunctionFactory() {
            @Override
            public String getName() {
                return "concat";
            }

            @Override
            public int getNumberOfArguments() {
                return VAR_ARG;
            }

            @Override
            public Value create(List<Value> arguments) {
                return new Value() {

                    @Override
                    public String toString() {
                        return String.format("concat(%s)", arguments);
                    }

                    @Override
                    public String toDebugString(Event e, Context context) {
                        String joinedValues = util.formatJoin(e, context, ", ", "'~v'", arguments);
                        return util.format(e, context, "[[source:concat(~s)]]'~v'", joinedValues,
                                this);
                    }

                    @Override
                    protected String getImp(Event e, Context context) {
                        StringBuilder s = new StringBuilder();
                        for (Value value : arguments) {
                            s.append(value.asString(e, context));
                        }
                        return s.toString();
                    }
                };
            }
        };
    }

}
