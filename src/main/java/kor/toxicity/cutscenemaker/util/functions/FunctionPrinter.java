package kor.toxicity.cutscenemaker.util.functions;

import kor.toxicity.cutscenemaker.util.TextUtil;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FunctionPrinter {

    public static final String PERCENT = "%";
    private final Function<LivingEntity,String> apply;
    public final boolean ANY_MATCH;

    public FunctionPrinter(String s) {
        if (ConditionBuilder.isFunction(s)) {
            ANY_MATCH = true;
            apply = convert(s);
            return;
        }
        List<Function<LivingEntity,String>> print = new ArrayList<>();
        int loop = 0;
        for (String t : TextUtil.getInstance().split(s,PERCENT)) {
            String colored = TextUtil.getInstance().colored(t);
            loop ++;
            if (Math.floorMod(loop,2) == 0) {
                Function<LivingEntity,String> function = get(colored);
                if (function != null) print.add(function);
            } else print.add(q -> colored);
        }
        if (print.size() == 1) {
            ANY_MATCH = false;
            apply = print.get(0);
        } else {
            ANY_MATCH = true;
            StringBuilder t = new StringBuilder();
            apply = e -> {
                t.setLength(0);
                for (Function<LivingEntity, String> f : print) {
                    t.append(f.apply(e));
                }
                return t.toString();
            };
        }
    }

    public String print(LivingEntity e) {
        return apply.apply(e);
    }

    private String printNumber(double d) {
        return (d == Math.floor(d)) ? TextUtil.getInstance().applyComma(d) : String.format("%.2f", d);
    }

    private Function<LivingEntity,String> get(String t) {
        if (t.equals("")) return q -> PERCENT;
        else return convert(t);
    }
    private Function<LivingEntity,String> convert(String t) {
        Function<LivingEntity, ?> f = ConditionBuilder.LIVING_ENTITY.getAsFunc(t);
        if (f != null) {
            return e -> {
                Object o = f.apply(e);
                if (o instanceof Number) return printNumber(((Number) o).doubleValue());
                else return o.toString();
            };
        } else return null;
    }
}
