package com.seeyoo.mps.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.seeyoo.mps.bean.SimpleTerminalBean;
import com.seeyoo.mps.conn.Command;
import com.seeyoo.mps.conn.Dir;
import com.seeyoo.mps.conn.UserCommander;
import com.seeyoo.mps.controller.request.CmdRequest;
import com.seeyoo.mps.dao.*;
import com.seeyoo.mps.integration.mq.RabbitMqManager;
import com.seeyoo.mps.model.*;
import com.seeyoo.mps.service.CmdService;
import com.seeyoo.mps.service.SystemSettingService;
import com.seeyoo.mps.service.TaskRecordService;
import com.seeyoo.mps.service.TgroupService;
import com.seeyoo.mps.tool.ResultUtil;
import com.seeyoo.mps.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class CmdServiceImpl implements CmdService {
    @Autowired
    TerminalSettingRepository terminalSettingRepository;

    @Autowired
    TerminalRepository terminalRepository;

    @Autowired
    UserCommander userCommander;

    @Autowired
    TgroupService tgroupService;

    @Autowired
    RabbitMqManager rabbitMqManager;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    MediaRepository mediaRepository;

    @Autowired
    TaskRecordService taskRecordService;

    @Autowired
    WeatherRepository weatherRepository;

    @Autowired
    SystemSettingService systemSettingService;

    @Override
    public Result cmdStatus(String opNo) {
        List<Command> commands = new ArrayList<>();
        if (!StrUtil.isEmpty(opNo)) {
            commands = userCommander.getCommands(opNo);
        }
        int sendCnt = 0;
        int returnCnt = 0;
        int notSendCnt = 0;
        int total = 0;
        List<Map<String, Object>> list = new ArrayList<>();
        for (Command command : commands) {
            Map<String, Object> m = new HashMap<>();
            m.put("mac", command.getMac());
            m.put("name", command.getName());
            int sendStatus = command.getSendStatus();
            int returnStatus = command.getReturnStatus();
            if (sendStatus == 0) {
                notSendCnt++;
                m.put("status", 0);
            }

            if (returnStatus == 1) {
                returnCnt++;
                m.put("status", 1);
            }

            if (returnStatus != 1 && sendStatus == 1) {
                sendCnt++;
                m.put("status", 2);
            }

            total++;
            list.add(m);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("send", sendCnt);
        map.put("return", returnCnt);
        map.put("notSend", notSendCnt);
        map.put("rows", list);
        return new ResultUtil<>().setData(map);
    }

    private List<SimpleTerminalBean> getTerminals(Long tgroupId, Long[] terminalIds) {
        List<Object[]> terminals = new ArrayList<>();
        if (tgroupId != null && tgroupId != 0) {
            String tgroupCode = tgroupService.tgroupCode(tgroupId);
            if (StrUtil.isEmpty(tgroupCode)) {
                return new ArrayList<>();
            }
            terminals = terminalRepository.terminalsByTgroupCode(tgroupCode);
        } else if (terminalIds != null) {
            terminals = terminalRepository.terminalsByIds(terminalIds);
        }

        List<SimpleTerminalBean> terminalBeans = new ArrayList<>();
        for (Object[] ter : terminals) {
            terminalBeans.add(new SimpleTerminalBean((Long) ter[0], String.valueOf(ter[1]), String.valueOf(ter[2])));
        }
        return terminalBeans;
    }

    @Override
    public Result reboot(Long userId, CmdRequest cmdRequest) {
        String opNo = IdUtil.simpleUUID();

        List<SimpleTerminalBean> terminals = getTerminals(cmdRequest.getTgroupId(), cmdRequest.getTerminalIds());
        for (SimpleTerminalBean terminal : terminals) {
            String msgId = opNo + terminal.getMac();
            String dir = Dir.CT_POWERRST(new String[]{msgId, "3"});
            userCommander.send(terminal.getId(), terminal.getMac(), terminal.getName(), msgId, opNo, (short) 7, dir, userId);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("opNo", opNo);
        return new ResultUtil<>().setData(map);
    }

    @Override
    public Result setTime(Long userId, CmdRequest cmdRequest) {
        String opNo = IdUtil.simpleUUID();

        List<SimpleTerminalBean> terminals = getTerminals(cmdRequest.getTgroupId(), cmdRequest.getTerminalIds());
        for (SimpleTerminalBean terminal : terminals) {
            String msgId = opNo + terminal.getMac();
            String dir = Dir.CT_SETTIME(new String[]{msgId});
            userCommander.send(terminal.getId(), terminal.getMac(), terminal.getName(), msgId, opNo, (short) 6, dir, userId);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("opNo", opNo);
        return new ResultUtil<>().setData(map);
    }

    @Override
    public Result getTime(Long userId, Long terminalId) {
        String opNo = IdUtil.simpleUUID();

        Terminal terminal = terminalRepository.findById(terminalId).orElse(null);
        if (terminal != null) {
            String msgId = opNo + terminal.getMac();
            String dir = Dir.CT_GETTIME(new String[]{msgId});
            userCommander.send(terminal.getId(), terminal.getMac(), terminal.getName(), msgId, opNo, (short) 34, dir, userId);
            String response = userCommander.take(msgId);
            if (!StrUtil.isEmpty(response)) {
                Map<String, Object> map = new HashMap<>();
                map.put("time", response);
                return new ResultUtil<>().setData(map);
            }
        } else {
            log.info("getTime not exist:{}", terminalId);
        }

        return new ResultUtil<>().setErrorMsg("获取失败");
    }

    @Override
    public Result setOnOff(Long userId, CmdRequest cmdRequest) {
        String opNo = IdUtil.simpleUUID();
        if (StrUtil.isEmpty(cmdRequest.getParam())) {
            return new ResultUtil<>().setErrorMsg("无效的参数");
        }

        StringBuilder arg = new StringBuilder();
        JSONObject params = new JSONObject(cmdRequest.getParam());
        JSONObject daySetting = params.optJSONObject("day");
        if (daySetting != null) {
            for (int i = 1; i <= 5; i++) {
                String day = daySetting.optString("d" + i, "00:00:00-00:00:00");
                String[] days = day.split("-");
                String on = "<On" + i + ">00:00:00</On" + i + ">";
                String off = "<Off" + i + ">00:00:00</Off" + i + ">";
                if (days.length == 2) {
                    if (days[0].length() == 8) {
                        on = "<On" + i + ">" + days[0] + "</On" + i + ">";
                    }
                    if (days[1].length() == 8) {
                        on = "<Off" + i + ">" + days[1] + "</Off" + i + ">";
                    }
                }
                arg.append(on);
                arg.append(off);
            }
        }

        JSONObject weekSetting = params.optJSONObject("week");
        if (daySetting != null) {
            for (int i = 1; i <= 7; i++) {
                String week = weekSetting.optString("w" + i, "00:00-00:00");
                arg.append("<W" + i + ">" + week + "</W" + i + ">");
            }
        }

        JSONObject holidaySetting = params.optJSONObject("holiday");
        if (daySetting != null) {
            for (int i = 1; i <= 8; i++) {
                String holiday = holidaySetting.optString("h" + i, "00-00 00:00-00:00");
                arg.append("<D" + i + ">" + holiday + "</D" + i + ">");
            }
        }

        List<SimpleTerminalBean> terminals = getTerminals(cmdRequest.getTgroupId(), cmdRequest.getTerminalIds());
        for (SimpleTerminalBean terminal : terminals) {
            String msgId = opNo + terminal.getMac();
            String dir = Dir.CT_SETONOFFTIME(new String[]{msgId, arg.toString()});
            userCommander.send(terminal.getId(), terminal.getMac(), terminal.getName(), msgId, opNo, (short) 8, dir, userId);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("opNo", opNo);
        return new ResultUtil<>().setData(map);
    }

    @Override
    public Result getOnOff(Long userId, Long terminalId) {
        String opNo = IdUtil.simpleUUID();

        Terminal terminal = terminalRepository.findById(terminalId).orElse(null);
        if (terminal != null) {
            String msgId = opNo + terminal.getMac();
            String dir = Dir.CT_GETONOFF(new String[]{msgId});
            userCommander.send(terminal.getId(), terminal.getMac(), terminal.getName(), msgId, opNo, (short) 31, dir, userId);
            String response = userCommander.take(msgId);
            if (!StrUtil.isEmpty(response)) {
                JSONObject res = new JSONObject(response);
                return new ResultUtil<>().setData(res.toMap());
            }
        } else {
            log.info("getOnOff not exist:{}", terminalId);
        }

        return new ResultUtil<>().setErrorMsg("获取失败");
    }

    @Override
    public Result setServer(Long userId, CmdRequest cmdRequest) {
        String opNo = IdUtil.simpleUUID();
        if (StrUtil.isEmpty(cmdRequest.getParam())) {
            return new ResultUtil<>().setErrorMsg("无效的参数");
        }

        JSONObject params = new JSONObject(cmdRequest.getParam());
        String mcsAddr = params.optString("mIp");
        String mcsDomain = params.optString("mDomain");
        String mcsPort = params.optString("mPort");
        String subAddr = params.optString("sIp");
        String subDomain = params.optString("sDomain");
        String subPort = params.optString("sPort");
        String mainFtpAddr = params.optString("mFtp");
        String mainFtpDomain = params.optString("mFDomain");
        String mainFtpPort = params.optString("mFPort");
        String mainFtpUser = params.optString("mFUser");
        String mainFtpPasswd = params.optString("mFPass");
        String subFtpAddr = params.optString("sFtp");
        String subFtpDomain = params.optString("sFDomain");
        String subFtpPort = params.optString("sFPort");
        String subFtpUser = params.optString("sFUser");
        String subFtpPasswd = params.optString("sFPass");
        String trackAddr = params.optString("tAddr");
        String trackPort = params.optString("tPort");
        String satAddr = params.optString("satAddr");

        List<SimpleTerminalBean> terminals = getTerminals(cmdRequest.getTgroupId(), cmdRequest.getTerminalIds());
        for (SimpleTerminalBean terminal : terminals) {
            String msgId = opNo + terminal.getMac();
            String dir = Dir.CT_SETSERVER(new String[]{msgId, mcsAddr, mcsDomain, mcsPort, mainFtpAddr,
                    mainFtpDomain, mainFtpPort, mainFtpUser, mainFtpPasswd, subAddr, subDomain, subPort, subFtpAddr,
                    subFtpDomain, subFtpPort, subFtpUser, subFtpPasswd, trackAddr, trackPort, satAddr});
            userCommander.send(terminal.getId(), terminal.getMac(), terminal.getName(), msgId, opNo, (short) 3, dir, userId);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("opNo", opNo);
        return new ResultUtil<>().setData(map);
    }

    @Override
    public Result getServer(Long userId, Long terminalId) {
        String opNo = IdUtil.simpleUUID();

        Terminal terminal = terminalRepository.findById(terminalId).orElse(null);
        if (terminal != null) {
            String msgId = opNo + terminal.getMac();
            String dir = Dir.CT_GETSERVER(new String[]{msgId});
            userCommander.send(terminal.getId(), terminal.getMac(), terminal.getName(), msgId, opNo, (short) 30, dir, userId);
            String response = userCommander.take(msgId);
            if (!StrUtil.isEmpty(response)) {
                JSONObject res = new JSONObject(response);
                return new ResultUtil<>().setData(res.toMap());
            }
        } else {
            log.info("getServer not exist:{}", terminalId);
        }

        return new ResultUtil<>().setErrorMsg("获取失败");
    }

    @Override
    @Transactional
    public Result sendTask(Long userId, CmdRequest cmdRequest) {
        String opNo = IdUtil.simpleUUID();
        if (StrUtil.isEmpty(cmdRequest.getParam())) {
            return new ResultUtil<>().setErrorMsg("无效的参数");
        }

        JSONObject params = new JSONObject(cmdRequest.getParam());
        Long taskId = params.optLong("taskId");
        if (taskId == 0) {
            return new ResultUtil<>().setErrorMsg("无效的任务");
        }
        log.info("{}", taskId);
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return new ResultUtil<>().setErrorMsg("无效的任务");
        }

        List<SimpleTerminalBean> terminals = getTerminals(cmdRequest.getTgroupId(), cmdRequest.getTerminalIds());
        for (SimpleTerminalBean terminal : terminals) {
            TerminalSetting terminalSetting = new TerminalSetting();
            Terminal t = new Terminal();
            t.setId(terminal.getId());
            TerminalSettingKey terminalSettingKey = new TerminalSettingKey(t, TerminalSettingEnum.TASK);
            terminalSetting.setTerminalSettingKey(terminalSettingKey);
            terminalSetting.setSettings(taskId + "");
            terminalSettingRepository.save(terminalSetting);
            rabbitMqManager.sendTask(terminal.getId());
        }
        taskRecordService.saveTaskRecord(userId, TaskRecordEnum.TASK, task.getId(), task.getName(), task.getStart(), task.getEnd(), terminals);
        Map<String, Object> map = new HashMap<>();
        map.put("opNo", opNo);
        return new ResultUtil<>().setData(map);
    }

    @Override
    public Result sendDefaultTask(Long userId, CmdRequest cmdRequest) {
        String opNo = IdUtil.simpleUUID();
        if (StrUtil.isEmpty(cmdRequest.getParam())) {
            return new ResultUtil<>().setErrorMsg("无效的参数");
        }

        JSONObject params = new JSONObject(cmdRequest.getParam());
        Long taskId = params.optLong("taskId", 0);
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return new ResultUtil<>().setErrorMsg("无效的任务");
        }

        List<SimpleTerminalBean> terminals = getTerminals(cmdRequest.getTgroupId(), cmdRequest.getTerminalIds());
        for (SimpleTerminalBean terminal : terminals) {
            TerminalSetting terminalSetting = new TerminalSetting();
            Terminal t = new Terminal();
            t.setId(terminal.getId());
            TerminalSettingKey terminalSettingKey = new TerminalSettingKey(t, TerminalSettingEnum.DEFAULTTASK);
            terminalSetting.setTerminalSettingKey(terminalSettingKey);
            terminalSetting.setSettings(taskId + "");
            terminalSettingRepository.save(terminalSetting);
            rabbitMqManager.sendTask(terminal.getId());
        }
        taskRecordService.saveTaskRecord(userId, TaskRecordEnum.DEFAULT_TASK, task.getId(), task.getName(), task.getStart(), task.getEnd(), terminals);
        Map<String, Object> map = new HashMap<>();
        map.put("opNo", opNo);
        return new ResultUtil<>().setData(map);
    }

    @Override
    public Result setCity(Long userId, CmdRequest cmdRequest) {
        String opNo = IdUtil.simpleUUID();
        if (StrUtil.isEmpty(cmdRequest.getParam())) {
            return new ResultUtil<>().setErrorMsg("无效的参数");
        }

        JSONObject params = new JSONObject(cmdRequest.getParam());
        Long city = params.optLong("city", 0);
        if (city == 0) {
            return new ResultUtil<>().setErrorMsg("无效的城市");
        }

        Weather weather = weatherRepository.findById(city).orElse(null);
        if (weather == null) {
            new ResultUtil<>().setErrorMsg("无效的参数");
        }

        String serverUrl = systemSettingService.getSetting("server_url", "http://localhost:9999/");
        List<SimpleTerminalBean> terminals = getTerminals(cmdRequest.getTgroupId(), cmdRequest.getTerminalIds());
        for (SimpleTerminalBean terminal : terminals) {
            TerminalSetting terminalSetting = new TerminalSetting();
            Terminal t = new Terminal();
            t.setId(terminal.getId());
            TerminalSettingKey terminalSettingKey = new TerminalSettingKey(t, TerminalSettingEnum.CITY);
            terminalSetting.setTerminalSettingKey(terminalSettingKey);
            terminalSetting.setSettings(weather.getWeatherId() + "");
            terminalSettingRepository.save(terminalSetting);

            String msgId = opNo + terminal.getMac();
            String dir = Dir.CT_SETWEATHER(new String[]{msgId, weather.getWeatherId() + "", weather.getCityName(), serverUrl});
            userCommander.send(terminal.getId(), terminal.getMac(), terminal.getName(), msgId, opNo, (short) 99, dir, userId);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("opNo", opNo);
        return new ResultUtil<>().setData(map);
    }

    @Override
    public Result getCity(Long userId, Long terminalId) {
        Long weatherId = 0l;
        try {
            Terminal terminal = new Terminal();
            terminal.setId(terminalId);
            TerminalSetting terminalSetting = terminalSettingRepository.findById(new TerminalSettingKey(terminal, TerminalSettingEnum.CITY)).orElse(null);
            if (terminalSetting != null) {
                List<Weather> weather = weatherRepository.findAllByWeatherId(Integer.valueOf(terminalSetting.getSettings()));
                if (weather != null) {
                    weatherId = weather.get(0).getId();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("city", weatherId);
        return new ResultUtil<>().setData(map);
    }

    @Override
    public Result checkTask(Long userId, Long terminalId) {
        String opNo = IdUtil.simpleUUID();

        Terminal terminal = terminalRepository.findById(terminalId).orElse(null);
        if (terminal != null) {
            String msgId = opNo + terminal.getMac();
            String dir = Dir.CT_TASKFILESTATUS(new String[]{msgId, "1"});
            userCommander.send(terminal.getId(), terminal.getMac(), terminal.getName(), msgId, opNo, (short) 46, dir, userId);
            String response = userCommander.take(msgId);
            if (!StrUtil.isEmpty(response)) {
                Map<String, Object> map = new HashMap<>();

                List<HashMap<String, Object>> files = new ArrayList<>();
                JSONArray fileArray = new JSONArray(response);
                for (int i = 0; i < fileArray.length(); i++) {
                    JSONArray f = fileArray.optJSONArray(i);
                    HashMap<String, Object> file = new HashMap<>();
                    file.put("name", f.get(0));
                    file.put("status", f.get(1));
                    files.add(file);
                }
                map.put("files", files);
                return new ResultUtil<>().setData(map);
            }
        } else {
            log.info("checkTask not exist:{}", terminalId);
        }

        return new ResultUtil<>().setErrorMsg("获取失败");
    }

    @Override
    @Transactional
    public Result sendUpgrade(Long userId, CmdRequest cmdRequest) {
        String opNo = IdUtil.simpleUUID();
        if (StrUtil.isEmpty(cmdRequest.getParam())) {
            return new ResultUtil<>().setErrorMsg("无效的参数");
        }

        JSONObject params = new JSONObject(cmdRequest.getParam());
        Long id = params.optLong("id", 0);
        if (id == 0) {
            return new ResultUtil<>().setErrorMsg("无效的文件");
        }
        Media media = mediaRepository.findById(id).orElse(null);
        if (media == null) {
            return new ResultUtil<>().setErrorMsg("无效的文件");
        }

        if (media.getType().intValue() != 5) {
            return new ResultUtil<>().setErrorMsg("无效的文件");
        }
        List<SimpleTerminalBean> terminals = getTerminals(cmdRequest.getTgroupId(), cmdRequest.getTerminalIds());
        for (SimpleTerminalBean terminal : terminals) {
            String msgId = opNo + terminal.getMac();
            String dir = Dir.CT_UPDATE(new String[]{msgId, media.getType() + "", media.getPath()});
            userCommander.send(terminal.getId(), terminal.getMac(), terminal.getName(), msgId, opNo, (short) 25, dir, userId);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("opNo", opNo);
        return new ResultUtil<>().setData(map);
    }

    @Override
    public Result selfCmd(Long userId, Long terminalId, String cmd) {
        String opNo = IdUtil.simpleUUID();

        if (StrUtil.isEmpty(cmd)) {
            return new ResultUtil<>().setErrorMsg("无效的命令");
        }

        Terminal terminal = terminalRepository.findById(terminalId).orElse(null);
        if (terminal != null) {
            String msgId = opNo + terminal.getMac();
            String dir = Dir.CT_SELFCMD(new String[]{msgId, cmd});
            userCommander.send(terminal.getId(), terminal.getMac(), terminal.getName(), msgId, opNo, (short) 58, dir, userId);
            String response = userCommander.take(msgId);
            if (!StrUtil.isEmpty(response)) {
                try {
                    response = new String(Base64.decodeBase64(response), "UTF-8");
                } catch (Exception e) {
                }
            }
            Map<String, Object> map = new HashMap<>();
            map.put("response", response);
            return new ResultUtil<>().setData(response);
        } else {
            log.info("checkTask not exist:{}", terminalId);
        }

        return new ResultUtil<>().setErrorMsg("获取失败");
    }
}
