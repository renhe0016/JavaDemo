package com.itlaborer.apitools.ui;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.itlaborer.apitools.model.ApiDoc;
import com.itlaborer.apitools.model.ApiItem;
import com.itlaborer.apitools.model.ApiMod;
import com.itlaborer.apitools.model.ApiPar;
import com.itlaborer.apitools.model.ApiPar2;
import com.itlaborer.apitools.res.KeyCode;
import com.itlaborer.apitools.res.Resource;
import com.itlaborer.apitools.res.XinzhiWeather;
import com.itlaborer.apitools.swt.SWTResourceManager;
import com.itlaborer.apitools.utils.ParamUtils;
import com.itlaborer.apitools.utils.PubUtils;

import net.dongliu.requests.Parameter;
import net.dongliu.requests.RawResponse;

/**
 * 程序主界面
 * 
 * @author liudewei[793554262@qq.com]
 * @version 1.8
 * @since 1.0
 */

public class MainWindow {

	private static Logger logger = Logger.getLogger(MainWindow.class.getName());
	private String[] loadServerAdressArray;
	private String[] loadApiJsonFileArray;

	// 其他成员变量-
	private int httpCode, parsSum;
	// 参数顺序标志,0原始顺序,1正序,2倒序
	private int orderFlag;
	private long httpTime;
	private Properties properties;
	private String applicationName;
	private String serverAdress;
	private String apiJsonFile;
	private String interfaceContextPath;
	private String bodyReturnStr;
	private String headerReturnStr;
	private String settingReqCharSet;
	private String settingResCharSet;
	private String autoCheckResCharSet;
	private boolean keyDownFlag = false;
	private boolean windowFocusFlag = false;
	private boolean openByShortcutFlag = false;
	private ApiDoc apiDoc;
	protected LinkedHashMap<String, String> pubpar;
	protected LinkedHashMap<String, String> header;
	protected LinkedHashMap<String, String> cookies;
	private byte[] resultByte;
	private HashMap<String, ApiItem> tempSavePars;

	// 界面组件
	private final FormToolkit formToolkit;
	protected Shell mainWindowShell;
	private CTabFolder cTabFolder;
	private Button parsCovertButton;
	private Button parsClearButton;
	private Button toBrower;
	private Button charSetButton;
	private Combo methodSelectCombo;
	private Combo modSelectCombo;
	private Combo interfaceCombo;
	private StyledText resultBodyStyledText;
	private StyledText resultHeaderStyledText;
	private Text statusBar;
	private StyledText parsText;
	private Text urlText;
	private Button submitButton;
	private Button button;
	private Button textClearButton;
	private Button clearSpaceButton;
	private Table formTable;
	private StyledText reqStyledText;
	private Text[][] formPar;
	private Label[] label;
	private MenuItem[] menuItem1SubFrozen;
	private MenuItem serverSelect;
	private Menu servers;
	private MenuItem apiSelect;
	private Menu apis;
	private Button btnAuthorization;
	private Listener shortcutListener;
	private Listener shortcutListenerRecover;
	private Display display;

	// 文本搜索器
	private TextSearch textSearch;
	// 颜色
	private Color parBackgroundNormalColor;
	private Color parBackgroundSelectedColor;
	private Color parFontsFrozenColor;
	private Color parFontsnormalColor;

	// 定时任务提交相关
	private TimerTask requestTask;
	private Timer requestTimer;
	private long delay = 0;
	private long intevalPeriod = 1000;
	private long timerSum = -1;
	private long count = 0;
	private boolean timerIsRun = false;
	private String timerUrl;

	// ContentType
	private MenuItem contentTypeNull;
	private MenuItem contentTypexwwwForm;
	private MenuItem contentTypeJson;
	private MenuItem contentTypeJavaScript;
	private MenuItem contentTypeApplicationXml;
	private MenuItem contentTypeTextPlain;
	private MenuItem contentTypeTextXml;
	private MenuItem contentTypeTextHtml;

	// 主窗口
	public MainWindow() {
		/////////////////////////////////////////////////////////
		// 判断日志配置是否存在,不存在则创建默认日志配置
		File log4jFile = new File("./config/log4j.properties");
		if (!log4jFile.exists()) {
			try {
				PubUtils.saveToFile(log4jFile, Resource.LOG4J);
			} catch (Exception e) {
				logger.warn("异常", e);
			}
		}
		/////////////////////////////////////////////////////////
		PropertyConfigurator.configure("config/log4j.properties ");
		logger.info("程序启动,程序版本为:" + Resource.APIVERSION);
		this.formToolkit = new FormToolkit(Display.getDefault());
		this.orderFlag = 0;
		this.parsSum = 196;
		this.serverAdress = "";
		this.settingResCharSet = "auto";
		this.settingReqCharSet = "UTF-8";
		this.resultByte = null;
		this.pubpar = new LinkedHashMap<String, String>();
		this.cookies = new LinkedHashMap<String, String>();
		this.header = new LinkedHashMap<String, String>();
		this.tempSavePars = new HashMap<String, ApiItem>();
		this.header.put("User-Agent", "APITools-" + Resource.APIVERSION);
		this.header.put("SocksTimeout", "30000");
		this.header.put("ConnectTimeout", "30000");
		this.parBackgroundNormalColor = SWTResourceManager.getColor(SWT.COLOR_WHITE);
		this.parBackgroundSelectedColor = new Color(Display.getCurrent(), 227, 247, 255);
		this.parFontsFrozenColor = SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY);
		this.parFontsnormalColor = SWTResourceManager.getColor(SWT.COLOR_BLACK);
		this.requestTimer = new Timer(false);
	}

	// 从这里开始,不是么？小桥流水人家~
	public static void main(String[] args) {
		try {
			MainWindow window = new MainWindow();
			window.open(true, false);
		} catch (Exception e) {
			logger.error("异常", e);
		}
	}

	public void open(boolean mainWindowFlag, boolean openByShortcutFlag) {
		this.openByShortcutFlag = openByShortcutFlag;
		display = Display.getDefault();
		createContents(display);
		mainWindowShell.open();
		initSystem();
		while (!mainWindowShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		logger.info("再见~~~~~");
		display.removeFilter(SWT.KeyDown, shortcutListener);
		display.removeFilter(SWT.KeyUp, shortcutListenerRecover);
		if (mainWindowFlag) {
			System.exit(0);
		}
	}

	protected void createContents(final Display display) {
		applicationName = "APITools" + "-" + Resource.APIVERSION;
		mainWindowShell = new Shell(display, SWT.MIN);
		mainWindowShell.setSize(1145, 670);
		mainWindowShell.setText(applicationName);
		mainWindowShell.setImage(SWTResourceManager.getImage(MainWindow.class, Resource.IMAGE_ICON));
		PubUtils.setCenter(mainWindowShell);
		dropTargetSupport(mainWindowShell);
		// 菜单////////////////////////////////////////////////////////
		Menu rootMenu = new Menu(mainWindowShell, SWT.BAR);
		mainWindowShell.setMenuBar(rootMenu);

		// 工具菜单///////////////////////////////////////////////////
		/////////////////// 编辑////////////////////////////////////////
		MenuItem menuEdit = new MenuItem(rootMenu, SWT.CASCADE);
		menuEdit.setText("文件");

		Menu menuSave = new Menu(menuEdit);
		menuEdit.setMenu(menuSave);

		MenuItem menuItemCreateNewFile = new MenuItem(menuSave, SWT.NONE);
		menuItemCreateNewFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CreateFileDialog createFileDialog = new CreateFileDialog(mainWindowShell, SWT.CLOSE | SWT.SYSTEM_MODAL);
				Object[] fileInfo = createFileDialog.open();
				if ((boolean) fileInfo[0]) {
					String name = (String) fileInfo[1];
					String version = (String) fileInfo[2];
					String serverList = (String) fileInfo[3];
					ApiDoc newDoc = new ApiDoc();
					newDoc.setDecodeversion(1.1);
					newDoc.setVersion(version);
					newDoc.setName(name);
					newDoc.setServerlist(serverList);
					newDoc.setItem(new ArrayList<ApiMod>());
					if (PubUtils.saveToFile(new File("./config/" + name + "-" + version + ".json"),
							PubUtils.jsonFormat(JSON.toJSONString(newDoc, SerializerFeature.WriteNullStringAsEmpty)))) {
						statusBar.setText("保存成功,请重新配置程序配置文件后重启加载接口文档");
					} else {
						statusBar.setText("保存失败,请重试");
					}

				} else {
					logger.debug("放弃新增接口文档");
				}
			}
		});
		menuItemCreateNewFile.setText("新建接口文档（空接口文档）");

		MenuItem menuItemSave = new MenuItem(menuSave, SWT.NONE);
		menuItemSave.setText("保存当前接口参数（程序关闭前有效）");

		MenuItem menuItemSaveToFile = new MenuItem(menuSave, SWT.NONE);
		menuItemSaveToFile.setText("保存当前接口参数（保存到接口文档）");

		// API列表
		apiSelect = new MenuItem(rootMenu, SWT.CASCADE);
		apiSelect.setText("接口列表");
		apis = new Menu(apiSelect);
		apiSelect.setMenu(apis);

		// 服务器列表
		serverSelect = new MenuItem(rootMenu, SWT.CASCADE);
		serverSelect.setText("服务器列表");
		servers = new Menu(serverSelect);
		serverSelect.setMenu(servers);

		// 工具菜单///////////////////////////////////////////////////
		MenuItem menuToolKit = new MenuItem(rootMenu, SWT.CASCADE);
		menuToolKit.setText("工具集合");
		// 工具菜单子菜单
		Menu menu = new Menu(menuToolKit);
		menuToolKit.setMenu(menu);

		MenuItem menuItemCode = new MenuItem(menu, SWT.CASCADE);
		menuItemCode.setText("编码解码");

		Menu menu_1 = new Menu(menuItemCode);
		menuItemCode.setMenu(menu_1);

		MenuItem menuItemMd5 = new MenuItem(menu_1, SWT.NONE);
		menuItemMd5.setText("MD5加密");

		MenuItem menuItemUrl = new MenuItem(menu_1, SWT.NONE);
		menuItemUrl.setText("URL编码/解码");

		MenuItem menuItemBase64 = new MenuItem(menu_1, SWT.NONE);
		menuItemBase64.setText("Base64编码/解码");

		MenuItem menuItemUnicode = new MenuItem(menu_1, SWT.NONE);
		menuItemUnicode.setText("Unicode编码/解码");

		// Unicode工具
		menuItemUnicode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				UnicodeTools unicodeTools = new UnicodeTools(mainWindowShell, SWT.CLOSE | SWT.SYSTEM_MODAL);
				unicodeTools.open();
			}
		});

		// Base64工具
		menuItemBase64.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Base64Tools base64Tools = new Base64Tools(mainWindowShell, SWT.CLOSE | SWT.SYSTEM_MODAL);
				base64Tools.open();
			}
		});

		// 菜单选项事件
		menuItemUrl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				UrlEncodeTools urlEncodeTools = new UrlEncodeTools(mainWindowShell, SWT.CLOSE | SWT.SYSTEM_MODAL);
				urlEncodeTools.open();
			}
		});

		// MD5工具
		menuItemMd5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MD5Tools md5Tools = new MD5Tools(mainWindowShell, SWT.CLOSE | SWT.SYSTEM_MODAL);
				md5Tools.open();
			}
		});

		MenuItem menuPar = new MenuItem(menu, SWT.CASCADE);
		menuPar.setText("Header参数");
		Menu menu_2 = new Menu(menuPar);
		menuPar.setMenu(menu_2);

		MenuItem menuItemHeader = new MenuItem(menu_2, SWT.NONE);
		menuItemHeader.setText("Header");

		MenuItem menuItemCookie = new MenuItem(menu_2, SWT.NONE);
		menuItemCookie.setText("Cookie");
		// Header编辑器
		menuItemHeader.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PubParEdit headerEdit = new PubParEdit(mainWindowShell, "Header常规", SWT.CLOSE | SWT.SYSTEM_MODAL);
				header = headerEdit.open(header);
				logger.info("读取到Header:" + header);
			}
		});
		// Cookie编辑器
		menuItemCookie.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PubParEdit headerEdit = new PubParEdit(mainWindowShell, "Cookie", SWT.CLOSE | SWT.SYSTEM_MODAL);
				cookies = headerEdit.open(cookies);
				logger.info("读取到Cookie:" + cookies);
			}
		});

		MenuItem menuItemPubPar = new MenuItem(menu, SWT.NONE);
		menuItemPubPar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PubParEdit pubEdit = new PubParEdit(mainWindowShell, "公共", SWT.CLOSE | SWT.SYSTEM_MODAL);
				pubpar = pubEdit.open(pubpar);
				logger.info("读取到公共参数:" + pubpar);
				// 保存公共参数
				apiDoc.setPublicpars(pubpar);
				// 在新的线程异步保存
				new Thread(new Runnable() {
					@Override
					public void run() {
						PubUtils.saveToFile(new File("./config/" + apiJsonFile), PubUtils
								.jsonFormat(JSON.toJSONString(apiDoc, SerializerFeature.WriteNullStringAsEmpty)));
					}
				}).start();
				initPubParameters(pubpar);
			}
		});
		menuItemPubPar.setText("公共表单参数");

		MenuItem menuItemCreateNewWindow = new MenuItem(rootMenu, SWT.NONE);
		menuItemCreateNewWindow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				initNewWindow(false, false);
			}
		});
		menuItemCreateNewWindow.setText("应用分身");

		/////////////////// 帮助////////////////////////////////////////
		MenuItem menuHelp = new MenuItem(rootMenu, SWT.CASCADE);
		menuHelp.setText("帮助");

		Menu menu_3 = new Menu(menuHelp);
		menuHelp.setMenu(menu_3);

		MenuItem menuItemManual = new MenuItem(menu_3, SWT.NONE);
		menuItemManual.setText("查看手册");

		MenuItem menuItemFeedBack = new MenuItem(menu_3, SWT.NONE);
		menuItemFeedBack.setText("报告问题");
		// 快捷键说明
		MenuItem menuItemShortcutKey = new MenuItem(menu_3, SWT.NONE);
		menuItemShortcutKey.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ShortcutKeyExplain shortcutKeyExplain = new ShortcutKeyExplain(mainWindowShell,
						SWT.CLOSE | SWT.SYSTEM_MODAL);
				shortcutKeyExplain.open(
						"Ctrl+Q：提交请求\r\nCtrl+Enter：提交请求\r\nCtrl+L：清空结果\r\nCtrl+S：保存参数到文件\r\nCtrl+N：打开一个新窗口\r\nCtrl+F:相应内容搜索");
			}
		});
		menuItemShortcutKey.setText("快捷键");
		// 菜单项-关于
		MenuItem menuItemAbout = new MenuItem(menu_3, SWT.NONE);
		menuItemAbout.setText("关于");
		// 模块选择
		modSelectCombo = new Combo(mainWindowShell, SWT.READ_ONLY);
		modSelectCombo.setBounds(3, 3, 230, 25);
		formToolkit.adapt(modSelectCombo);

		Menu menu_6 = new Menu(modSelectCombo);
		modSelectCombo.setMenu(menu_6);

		MenuItem menuItemCopyModName = new MenuItem(menu_6, SWT.NONE);
		menuItemCopyModName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Clipboard clipboard = new Clipboard(modSelectCombo.getDisplay());
				TextTransfer textTransfer = TextTransfer.getInstance();
				if (StringUtils.isNotEmpty(modSelectCombo.getText())) {
					clipboard.setContents(new String[] { modSelectCombo.getText() }, new Transfer[] { textTransfer });
				}
				clipboard.dispose();
				statusBar.setText("已复制到剪切板:" + modSelectCombo.getText());
			}
		});
		menuItemCopyModName.setText("复制模块名");

		MenuItem menuItemEditMod = new MenuItem(menu_6, SWT.NONE);
		menuItemEditMod.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (StringUtils.isEmpty(modSelectCombo.getText())) {
					statusBar.setText("不能编辑不存在的模块");
					return;
				}
				EditModDialog editModDialog = new EditModDialog(mainWindowShell, SWT.CLOSE | SWT.SYSTEM_MODAL);
				Object[] result = editModDialog.open(modSelectCombo.getText(), modSelectCombo.getToolTipText());
				if ((boolean) result[0]) {
					// 更新模块名
					String nameFromDialog = (String) result[1];
					StringBuffer stringBuffer = new StringBuffer();
					if ((!StringUtils.equals(nameFromDialog, modSelectCombo.getText()))
							&& StringUtils.isNotEmpty(nameFromDialog)) {
						// 重名判断
						boolean flag = false;
						for (int i = 0; i < apiDoc.getItem().size(); i++) {
							if (StringUtils.equals(apiDoc.getItem().get(i).getName(), nameFromDialog)
									&& (i != modSelectCombo.getSelectionIndex())) {
								flag = true;
								break;
							}
						}
						if (flag) {
							logger.debug("重命名模块时发生重名");
							stringBuffer.append("重命名时发现重名模块,放弃重命名");
						} else {
							apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).setName(nameFromDialog);
							stringBuffer.append("模块名被修改为:" + nameFromDialog);
						}
					}
					// 更新备注
					String desFromDialog = (String) result[2];
					if (!StringUtils.equals(nameFromDialog, modSelectCombo.getToolTipText())) {
						apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).setDescription(desFromDialog);
						if (StringUtils.isNotEmpty(stringBuffer)) {
							stringBuffer.append("/模块备注被修改为:" + desFromDialog);
						} else {
							stringBuffer.append("模块备注被修改为:" + desFromDialog);
						}
					}
					// 保存更新
					if (PubUtils.saveToFile(new File("./config/" + apiJsonFile),
							PubUtils.jsonFormat(JSON.toJSONString(apiDoc, SerializerFeature.WriteNullStringAsEmpty)))) {
						modSelectCombo.setItem(modSelectCombo.getSelectionIndex(),
								apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getName() + "");
						modSelectCombo.setToolTipText(
								apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getDescription() + "");
					} else {
						statusBar.setText("编辑失败,请重试");
					}
				} else {
					logger.debug("放弃编辑");
				}
			}
		});
		menuItemEditMod.setText("编辑此模块");

		MenuItem menuItemDeleteMod = new MenuItem(menu_6, SWT.NONE);
		menuItemDeleteMod.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (StringUtils.isEmpty(modSelectCombo.getText())) {
					statusBar.setText("不能删除不存在的模块");
					return;
				}
				MySelectionDialog mySelectionDialog = new MySelectionDialog(mainWindowShell,
						SWT.CLOSE | SWT.SYSTEM_MODAL, "确定要删除此模块吗？删除后此模块下的接口也将删除,并且将无法恢复");
				boolean flag = mySelectionDialog.open();
				if (flag && (modSelectCombo.getSelectionIndex() != -1)) {
					try {
						int modindex = modSelectCombo.getSelectionIndex();
						logger.debug("开始删除模块:" + modSelectCombo.getText());
						// 移除
						apiDoc.getItem().remove(modindex);
						// 保存
						PubUtils.saveToFile(new File("./config/" + apiJsonFile), PubUtils
								.jsonFormat(JSON.toJSONString(apiDoc, SerializerFeature.WriteNullStringAsEmpty)));
						// 重新初始化界面
						modSelectCombo.remove(modindex);
						if (modSelectCombo.getItemCount() == 0) {
							logger.debug("模块被删光了");
							mainWindowShell.setText(applicationName);
							clearParameters();
							urlText.setText("");
							interfaceCombo.removeAll();
						}
						// 删除的是最后一个,则初始化倒数第二个
						else if (modindex == modSelectCombo.getItemCount()) {
							modSelectCombo.select(modindex - 1);
							initSelectMod(modindex - 1);
						} else {
							modSelectCombo.select(modindex);
							initSelectMod(modindex);
						}
						logger.debug("删除完成");
						statusBar.setText("删除完成");
					} catch (Exception e2) {
						logger.debug("删除时发生异常", e2);
						statusBar.setText("删除失败");
					}

				} else if (interfaceCombo.getSelectionIndex() == -1) {
					statusBar.setText("没有模块可供删除");
				} else {
					logger.debug("放弃删除模块:" + modSelectCombo.getText());
				}
			}
		});
		menuItemDeleteMod.setText("删除此模块");

		MenuItem menuItemCreateNewMod = new MenuItem(menu_6, SWT.NONE);
		menuItemCreateNewMod.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (null == apiDoc) {
					logger.info("不能在空的接口文档上创建模块,请先创建接口文档");
					statusBar.setText("不能在空的接口文档上创建模块,请先创建接口文档");
					return;
				}
				String modname;
				String description;
				CreateModDialog createModDialog = new CreateModDialog(mainWindowShell, SWT.CLOSE | SWT.SYSTEM_MODAL);
				Object[] res = createModDialog.open();
				if ((boolean) res[0]) {
					modname = (String) res[1];
					description = (String) res[2];
					if (StringUtils.isEmpty(modname)) {
						logger.debug("模块名为空,放弃添加");
						statusBar.setText("不能创建名字为空的模块");
					} else {
						// 重名判断
						boolean flag = false;
						for (int i = 0; i < apiDoc.getItem().size(); i++) {
							if (StringUtils.equals(apiDoc.getItem().get(i).getName(), modname)
									&& (i != modSelectCombo.getSelectionIndex())) {
								flag = true;
								break;
							}
						}
						if (flag) {
							logger.debug("不能添加重名的模块");
							statusBar.setText("不能添加重名的模块");
						} else {
							modSelectCombo.add(modname);
							ApiMod apiList = new ApiMod();
							apiList.setName(modname);
							apiList.setDescription(description);
							apiDoc.getItem().add(apiList);
							try {
								PubUtils.saveToFile(new File("./config/" + apiJsonFile), PubUtils.jsonFormat(
										JSON.toJSONString(apiDoc, SerializerFeature.WriteNullStringAsEmpty)));
								modSelectCombo.select(modSelectCombo.getItemCount() - 1);
								initSelectMod(modSelectCombo.getItemCount() - 1);
							} catch (Exception e2) {
								logger.debug("创建新模块写入报错", e2);
							}
						}
					}
				} else {
					logger.debug("放弃新增模块");
				}
			}
		});
		menuItemCreateNewMod.setText("新增一个模块");

		// 接口选择
		interfaceCombo = new Combo(mainWindowShell, SWT.READ_ONLY);
		interfaceCombo.setBounds(237, 3, 245, 25);
		formToolkit.adapt(interfaceCombo);

		Menu menu_4 = new Menu(interfaceCombo);
		interfaceCombo.setMenu(menu_4);

		MenuItem menuItem_2 = new MenuItem(menu_4, SWT.NONE);
		menuItem_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ArrayList<HashMap<String, String>> queryList = new ArrayList<HashMap<String, String>>();
				ApiMod apiMod = apiDoc.getItem().get(modSelectCombo.getSelectionIndex());
				if (null != apiMod && null != apiMod.getItem() && apiMod.getItem().size() > 0) {
					for (int a = 0; a < apiMod.getItem().size(); a++) {
						HashMap<String, String> hashMap = new HashMap<>();
						hashMap.put("name", apiMod.getItem().get(a).getName());
						hashMap.put("uuid", apiMod.getItem().get(a).getUuid());
						queryList.add(hashMap);
					}
					// 打开查询窗口
					InterfaceSearch interfaceSearch = new InterfaceSearch(mainWindowShell,
							SWT.CLOSE | SWT.SYSTEM_MODAL);
					Object[] result = interfaceSearch.open(queryList);
					if ((boolean) result[0]) {
						String uuid = (String) (result[1]);
						for (int a = 0; a < apiMod.getItem().size(); a++) {
							if (StringUtils.equals(apiMod.getItem().get(a).getUuid(), uuid)) {
								interfaceCombo.select(a);
								initSelectInterface(modSelectCombo.getSelectionIndex(), a);
								break;
							}
						}
					}
				}
			}
		});
		menuItem_2.setText("查找接口");

		MenuItem menuItemCopyInterfaceName = new MenuItem(menu_4, SWT.NONE);
		menuItemCopyInterfaceName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Clipboard clipboard = new Clipboard(interfaceCombo.getDisplay());
				TextTransfer textTransfer = TextTransfer.getInstance();
				if (StringUtils.isNotEmpty(interfaceCombo.getText())) {
					clipboard.setContents(new String[] { interfaceCombo.getText() }, new Transfer[] { textTransfer });
				}
				clipboard.dispose();
				statusBar.setText("已复制到剪切板:" + interfaceCombo.getText());
			}
		});
		menuItemCopyInterfaceName.setText("复制接口名");

		MenuItem menuItemEditInterface1 = new MenuItem(menu_4, SWT.NONE);
		menuItemEditInterface1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (StringUtils.isEmpty(interfaceCombo.getText())) {
					statusBar.setText("不能编辑不存在的接口");
					return;
				}
				EditCollectionDialog editCollectionDialog = new EditCollectionDialog(mainWindowShell,
						SWT.CLOSE | SWT.SYSTEM_MODAL);
				Object[] result = editCollectionDialog.open(interfaceCombo.getText(), interfaceCombo.getToolTipText(),
						serverAdress, interfaceContextPath, methodSelectCombo.getText());
				StringBuffer stringBuffer = new StringBuffer();
				if ((boolean) result[0]) {
					String nameFromDialog = (String) result[1];
					// 处理接口名变化
					if (StringUtils.isNotEmpty(nameFromDialog)
							&& (!StringUtils.equals(nameFromDialog, interfaceCombo.getText()))) {
						// 重名判断
						boolean flag = false;
						ArrayList<ApiItem> items = apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem();
						for (int i = 0; i < items.size(); i++) {
							if (StringUtils.equals(items.get(i).getName(), nameFromDialog)
									&& (i != interfaceCombo.getSelectionIndex())) {
								flag = true;
								break;
							}
						}
						if (flag) {
							logger.debug("本模块下有重名接口名,放弃重命名");
							stringBuffer.append("本模块下有重名接口名,放弃重命名");
						} else {
							apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem()
									.get(interfaceCombo.getSelectionIndex()).setName(nameFromDialog);
							stringBuffer.append("接口名被修改为:" + nameFromDialog);
						}
					}
					// 处理接口提示变化
					String desFromDialog = (String) result[2];
					if (!StringUtils.equals(desFromDialog, interfaceCombo.getToolTipText())) {
						apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem()
								.get(interfaceCombo.getSelectionIndex()).setDescription(desFromDialog);
						if (StringUtils.isEmpty(stringBuffer)) {
							stringBuffer.append("接口提示修改为:" + desFromDialog);
						} else {
							stringBuffer.append("/接口提示修改为:" + desFromDialog);
						}
					}
					// 处理接口路径变化
					String pathFromDialog = (String) result[3];
					if (!StringUtils.equals(pathFromDialog, interfaceContextPath)) {
						apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem()
								.get(interfaceCombo.getSelectionIndex()).setPath(pathFromDialog);
						if (StringUtils.isEmpty(stringBuffer)) {
							stringBuffer.append("接口路径修改为:" + pathFromDialog);
						} else {
							stringBuffer.append("/接口路径修改为:" + pathFromDialog);
						}
					}
					// 处理接口方法变化
					String methodFromDialog = (String) result[4];
					if (!StringUtils.equals(methodFromDialog, methodSelectCombo.getText())) {
						apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem()
								.get(interfaceCombo.getSelectionIndex()).setMethod(methodFromDialog);
						if (StringUtils.isEmpty(stringBuffer)) {
							stringBuffer.append("接口方法修改为:" + methodFromDialog);
						} else {
							stringBuffer.append("/接口方法修改为:" + methodFromDialog);
						}
					}
					// 保存变化--并更新UI
					if (PubUtils.saveToFile(new File("./config/" + apiJsonFile),
							PubUtils.jsonFormat(JSON.toJSONString(apiDoc, SerializerFeature.WriteNullStringAsEmpty)))) {
						ApiItem apiItem = apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem()
								.get(interfaceCombo.getSelectionIndex());
						interfaceCombo.setItem(interfaceCombo.getSelectionIndex(), apiItem.getName());
						if (StringUtils.isEmpty(desFromDialog)) {
							interfaceCombo.setToolTipText("");
						} else {
							interfaceCombo.setToolTipText(apiItem.getDescription());
						}
						interfaceContextPath = apiItem.getPath();
						urlText.setText(serverAdress + interfaceContextPath);
						methodChoice(methodFromDialog);
						statusBar.setText(stringBuffer.toString());
					} else {
						statusBar.setText("编辑保存失败,请重试");
					}
				} else {
					logger.debug("放弃编辑接口");
				}
			}
		});
		menuItemEditInterface1.setText("编辑此接口");

		MenuItem menuItemDeleteInterface = new MenuItem(menu_4, SWT.NONE);
		menuItemDeleteInterface.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (StringUtils.isEmpty(interfaceCombo.getText())) {
					statusBar.setText("不能删除不存在的接口");
					return;
				}
				MySelectionDialog mySelectionDialog = new MySelectionDialog(mainWindowShell,
						SWT.CLOSE | SWT.SYSTEM_MODAL, "确定要删除此接口吗？删除后将无法恢复");
				boolean flag = mySelectionDialog.open();
				if (flag && (interfaceCombo.getSelectionIndex() != -1)) {
					try {
						int modindex = modSelectCombo.getSelectionIndex();
						int interfaceindex = interfaceCombo.getSelectionIndex();
						logger.debug("开始删除接口:" + modSelectCombo.getText() + "模块下的" + interfaceCombo.getText());
						// 移除
						apiDoc.getItem().get(modindex).getItem().remove(interfaceindex);
						// 保存--请注意,保存时会把之前保存到内存中的参数也更新到文档---
						PubUtils.saveToFile(new File("./config/" + apiJsonFile), PubUtils
								.jsonFormat(JSON.toJSONString(apiDoc, SerializerFeature.WriteNullStringAsEmpty)));
						// 重新初始化界面
						interfaceCombo.remove(interfaceindex);
						if (interfaceCombo.getItemCount() == 0) {
							logger.debug("此模块下的接口被删光了");
							mainWindowShell.setText(applicationName);
							clearParameters();
							urlText.setText("");
						}
						// 删除的是最后一个,则初始化倒数第二个
						else if (interfaceindex == interfaceCombo.getItemCount()) {
							interfaceCombo.select(interfaceindex - 1);
							initSelectInterface(modindex, interfaceindex - 1);
						} else {
							interfaceCombo.select(interfaceindex);
							initSelectInterface(modindex, interfaceindex);
						}
						logger.debug("删除完成");
						statusBar.setText("删除完成");
					} catch (Exception e2) {
						logger.debug("删除时发生异常", e2);
						statusBar.setText("删除失败");
					}

				} else if (interfaceCombo.getSelectionIndex() == -1) {
					statusBar.setText("没有接口可供删除");
				} else {
					logger.debug("放弃删除接口:" + modSelectCombo.getText() + "模块下的" + interfaceCombo.getText());
				}
			}
		});
		menuItemDeleteInterface.setText("删除此接口");

		MenuItem menuItemCreateNewInterface = new MenuItem(menu_4, SWT.NONE);
		menuItemCreateNewInterface.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (modSelectCombo.getItemCount() == 0) {
					logger.info("不能在空的模块上创建接口,请先创建模块");
					statusBar.setText("不能在空的模块上创建接口,请先创建模块");
					return;
				}
				String collectionName;
				String description;
				String path;
				CreateCollectionDialog createModDialog = new CreateCollectionDialog(mainWindowShell,
						SWT.CLOSE | SWT.SYSTEM_MODAL);
				Object[] res = createModDialog.open(serverAdress);
				if ((boolean) res[0]) {
					collectionName = (String) res[1];
					description = (String) res[2];
					path = (String) res[3];
					if (StringUtils.isEmpty(collectionName)) {
						logger.debug("接口名为空,放弃添加");
						statusBar.setText("不能创建名字为空的接口");
					} else {
						// 重名判断以及此模块是否为空
						boolean flag = false;
						ArrayList<ApiItem> items = apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem();
						if (null != items) {
							for (int i = 0; i < items.size(); i++) {
								if (StringUtils.equals(items.get(i).getName(), collectionName)
										&& (i != interfaceCombo.getSelectionIndex())) {
									flag = true;
									break;
								}
							}
						}
						// 如果模块为空,则创建接口list对象
						else {
							apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).setItem(new ArrayList<ApiItem>());
						}
						// 如果重名则提示,否则创建接口
						if (flag) {
							logger.debug("新增接口时发生重名");
							statusBar.setText("新增时发现重名接口,放弃新增,请重新添加");
						} else {
							// 新增接口项目
							ApiItem apiItem = new ApiItem();
							apiItem.setName(collectionName);
							apiItem.setDescription(description);
							apiItem.setPath(path);
							apiItem.setMethod("GET");
							apiItem.setUuid(PubUtils.getUUID());
							apiItem.setParameters(new ArrayList<ApiPar>());
							// 加入当前组
							apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem().add(apiItem);
							interfaceCombo.add(collectionName);
							// 保存
							PubUtils.saveToFile(new File("./config/" + apiJsonFile), PubUtils
									.jsonFormat(JSON.toJSONString(apiDoc, SerializerFeature.WriteNullStringAsEmpty)));
							// 切换到这个接口
							interfaceCombo.select(interfaceCombo.getItemCount() - 1);
							initSelectInterface(modSelectCombo.getSelectionIndex(),
									apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem().size() - 1);
						}
					}
				} else {
					logger.debug("放弃新增接口");
				}
			}
		});
		menuItemCreateNewInterface.setText("新增一个接口");
		// 表单
		parsText = new StyledText(mainWindowShell, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		parsText.setBounds(3, 32, 480, 52);
		PubUtils.styledTextAddContextMenu(parsText);
		// URL
		urlText = new Text(mainWindowShell, SWT.BORDER);
		urlText.setBounds(487, 3, 478, 25);
		// HTTP请求的方法下拉选择框
		methodSelectCombo = new Combo(mainWindowShell, SWT.DROP_DOWN | SWT.READ_ONLY);
		methodSelectCombo.setBounds(970, 3, 66, 25);
		formToolkit.adapt(methodSelectCombo);
		methodSelectCombo.add("GET", 0);
		methodSelectCombo.add("POST", 1);
		methodSelectCombo.add("HEAD", 2);
		methodSelectCombo.add("PUT", 3);
		methodSelectCombo.add("PATCH", 4);
		methodSelectCombo.add("DELETE", 5);

		Menu menu_5 = new Menu(methodSelectCombo);
		methodSelectCombo.setMenu(menu_5);

		contentTypeNull = new MenuItem(menu_5, SWT.NONE);
		contentTypeNull.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				statusBar.setText("切换Content-Type为:空");
				header.remove("Content-Type");
				// 禁用ContentType默认使用url参数的形式提交参数
				reqStyledText.setVisible(false);
				formTable.setVisible(true);
			}
		});
		contentTypeNull.setText("禁用Content-Type");

		contentTypexwwwForm = new MenuItem(menu_5, SWT.NONE);
		contentTypexwwwForm.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String contentType = contentTypexwwwForm.getText();
				header.put("Content-Type", contentType);
				statusBar.setText("切换Content-Type为:" + contentType);
				// 表单
				reqStyledText.setVisible(false);
				formTable.setVisible(true);
			}
		});
		contentTypexwwwForm.setText("application/x-www-form-urlencoded");
		contentTypexwwwForm.setEnabled(false);

		contentTypeJson = new MenuItem(menu_5, SWT.NONE);
		contentTypeJson.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String contentType = contentTypeJson.getText();
				header.put("Content-Type", contentType);
				statusBar.setText("切换Content-Type为:" + contentType);
				// RAW
				reqStyledText.setVisible(true);
				formTable.setVisible(false);
				reqStyledText.setText(PubUtils.jsonFormat(JSON.toJSONString(getParameters())));
			}
		});
		contentTypeJson.setText("application/json");
		contentTypeJson.setEnabled(false);

		contentTypeJavaScript = new MenuItem(menu_5, SWT.NONE);
		contentTypeJavaScript.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String contentType = contentTypeJavaScript.getText();
				header.put("Content-Type", contentType);
				statusBar.setText("切换Content-Type为:" + contentType);
				// RAW
				reqStyledText.setVisible(true);
				formTable.setVisible(false);
			}
		});
		contentTypeJavaScript.setText("application/javascript");
		contentTypeJavaScript.setEnabled(false);

		contentTypeApplicationXml = new MenuItem(menu_5, SWT.NONE);
		contentTypeApplicationXml.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String contentType = contentTypeApplicationXml.getText();
				header.put("Content-Type", contentType);
				statusBar.setText("切换Content-Type为:" + contentType);
				// RAW
				reqStyledText.setVisible(true);
				formTable.setVisible(false);
			}
		});
		contentTypeApplicationXml.setText("application/xml");
		contentTypeApplicationXml.setEnabled(false);

		contentTypeTextPlain = new MenuItem(menu_5, SWT.NONE);
		contentTypeTextPlain.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String contentType = contentTypeTextPlain.getText();
				header.put("Content-Type", contentType);
				statusBar.setText("切换Content-Type为:" + contentType);
				// RAW
				reqStyledText.setVisible(true);
				formTable.setVisible(false);
			}
		});
		contentTypeTextPlain.setText("text/plain");
		contentTypeTextPlain.setEnabled(false);

		contentTypeTextXml = new MenuItem(menu_5, SWT.NONE);
		contentTypeTextXml.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String contentType = contentTypeTextXml.getText();
				header.put("Content-Type", contentType);
				statusBar.setText("切换Content-Type为:" + contentType);
				// RAW
				reqStyledText.setVisible(true);
				formTable.setVisible(false);
			}
		});
		contentTypeTextXml.setText("text/xml");
		contentTypeTextXml.setEnabled(false);

		contentTypeTextHtml = new MenuItem(menu_5, SWT.NONE);
		contentTypeTextHtml.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String contentType = contentTypeTextHtml.getText();
				header.put("Content-Type", contentType);
				statusBar.setText("切换Content-Type为:" + contentType);
				// RAW
				reqStyledText.setVisible(true);
				formTable.setVisible(false);
			}
		});
		contentTypeTextHtml.setText("text/html");
		contentTypeTextHtml.setEnabled(false);

		// 提交按钮
		submitButton = new Button(mainWindowShell, SWT.NONE);
		submitButton.setBounds(1040, 2, 97, 27);
		submitButton.setText("提      交");

		Menu menuTimer = new Menu(submitButton);
		submitButton.setMenu(menuTimer);

		MenuItem menuTimerItem = new MenuItem(menuTimer, SWT.NONE);
		menuTimerItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 重置请求次数
				count = 0;
				timerUrl = urlText.getText();
				if (!timerIsRun) {
					timerIsRun = true;
				} else {
					logger.debug("任务已启动，先清除原任务后重新布置任务");
					requestTask.cancel();
				}
				// 开始配置定时任务
				requestTask = new TimerTask() {
					@Override
					public void run() {
						if (mainWindowShell.isDisposed()) {
							// 如果主窗口已经关闭，强制取消所有任务，避免残留后台进程
							requestTimer.cancel();
							requestTimer.purge();
						} else {
							mainWindowShell.getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									String url = urlText.getText();
									if (!StringUtils.equals(url, timerUrl)) {
										requestTask.cancel();
										statusBar.setText("接口地址发生变化,定时循环提交已终止");
									} else if (timerSum > 0 && count >= timerSum) {
										statusBar.setText("已完成" + count + "次请求,定时循环提交已终止");
									} else {
										count++;
										sentRequest();
									}
								}
							});
						}
					}
				};
				// 启动定时任务
				statusBar.setText("定时任务已配置," + (delay < 0 ? 0 : delay) + "毫秒后启动");
				requestTimer.scheduleAtFixedRate(requestTask, delay, intevalPeriod);
			}
		});
		menuTimerItem.setText("开启定时循环提交");

		MenuItem menuTimerItem_1 = new MenuItem(menuTimer, SWT.NONE);
		menuTimerItem_1.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (timerIsRun) {
					requestTask.cancel();
					timerIsRun = false;
					statusBar.setText("定时循环提交已关闭");
				}
			}
		});
		menuTimerItem_1.setText("终止定时循环提交");

		MenuItem menuTimerItem_3 = new MenuItem(menuTimer, SWT.NONE);
		menuTimerItem_3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TimerConfigDialog timerConfigDialog = new TimerConfigDialog(mainWindowShell,
						SWT.CLOSE | SWT.SYSTEM_MODAL);
				Object[] objects = timerConfigDialog.open(delay, intevalPeriod, timerSum);
				if ((boolean) objects[0] == true) {
					logger.debug("定时器延迟启动时间(毫秒):" + (long) objects[1] + ",每次请求间隔时长(毫秒):" + (long) objects[2] + ",次数限制:"
							+ (long) objects[3]);
					statusBar.setText("定时器延迟启动时间(毫秒):" + (long) objects[1] + ",每次请求间隔时长(毫秒):" + (long) objects[2]
							+ ",次数限制:" + (long) objects[3]);
					delay = (long) objects[1];
					intevalPeriod = (long) objects[2];
					timerSum = (long) objects[3];
				}
			}
		});
		menuTimerItem_3.setText("配置定时循环提交");

		// 参数转换
		parsCovertButton = new Button(mainWindowShell, SWT.NONE);
		parsCovertButton.setToolTipText("导入参数串到表单");
		parsCovertButton.setText("导入参数");
		parsCovertButton.setBounds(487, 31, 72, 27);
		formToolkit.adapt(parsCovertButton, true, true);

		// 重置参数
		parsClearButton = new Button(mainWindowShell, SWT.NONE);
		parsClearButton.setToolTipText("重置参数为接口文档中定义的参数");
		parsClearButton.setText("重置参数");
		parsClearButton.setBounds(562, 31, 72, 27);
		formToolkit.adapt(parsClearButton, true, true);

		// 排除空格
		clearSpaceButton = new Button(mainWindowShell, SWT.NONE);
		clearSpaceButton.setToolTipText("清除参数两头可能存在的空格");
		clearSpaceButton.setText("TRIM参数");
		clearSpaceButton.setBounds(637, 31, 72, 27);
		formToolkit.adapt(clearSpaceButton, true, true);

		// 重排参数
		button = new Button(mainWindowShell, SWT.NONE);
		button.setToolTipText("将参数重新从第一个表格按正序或者倒序重新排列");
		button.setText("参数排序");
		button.setBounds(712, 31, 72, 27);
		formToolkit.adapt(button, true, true);
		// 字符集设置
		charSetButton = new Button(mainWindowShell, SWT.NONE);
		charSetButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CharSetDialog charSetDialog = new CharSetDialog(mainWindowShell, SWT.CLOSE | SWT.SYSTEM_MODAL);
				Object[] objects = charSetDialog.open(settingReqCharSet, settingResCharSet);
				if ((boolean) objects[0] == true) {
					logger.debug("请求编码设置为:" + (String) objects[1] + ",响应编码设置为:" + (String) objects[2]);
					if (((String) objects[2]).equals("auto")) {
						statusBar.setText("请求编码设置为:" + (String) objects[1] + ",响应编码设置为:自动检测");
					} else {
						statusBar.setText("请求编码设置为:" + (String) objects[1] + ",响应编码设置为:" + (String) objects[2]);
					}
					settingReqCharSet = (String) objects[1];
					settingResCharSet = (String) objects[2];
				}
			}
		});
		charSetButton.setToolTipText("设置请求和响应的编码");
		charSetButton.setText("字符集设置");
		charSetButton.setBounds(787, 31, 83, 27);
		formToolkit.adapt(charSetButton, true, true);

		// auth
		btnAuthorization = new Button(mainWindowShell, SWT.NONE);
		btnAuthorization.setToolTipText("授权管理");
		btnAuthorization.setText("Authorization");
		btnAuthorization.setBounds(873, 31, 92, 27);
		formToolkit.adapt(btnAuthorization, true, true);
		btnAuthorization.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				statusBar.setText("此功能暂未实现");
			}
		});
		// 点击清除结果
		textClearButton = new Button(mainWindowShell, SWT.NONE);
		textClearButton.setToolTipText("清空结果内容");
		textClearButton.setText("清空结果");
		textClearButton.setBounds(968, 31, 69, 27);
		formToolkit.adapt(textClearButton, true, true);
		// 去浏览器
		toBrower = new Button(mainWindowShell, SWT.NONE);
		toBrower.setToolTipText("用HTTP GET方式在浏览器中请求接口");
		toBrower.setText("浏览器中GET");
		toBrower.setBounds(1040, 31, 97, 27);
		formToolkit.adapt(toBrower, true, true);

		Menu browerMenu = new Menu(toBrower);
		toBrower.setMenu(browerMenu);

		MenuItem mntmget = new MenuItem(browerMenu, SWT.NONE);
		mntmget.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (StringUtils.isEmpty(urlText.getText())) {
					return;
				}
				HashMap<String, String> pars = getParameters();
				parsText.setText(ParamUtils.mapToQuery(pars));
				String url = urlText.getText() + (pars.size() == 0 ? ("") : ("?" + ParamUtils.mapToQuery(pars)));
				///////////////////////////////////////////////////////////////////////////////////////////////////
				Clipboard clipboard = new Clipboard(mainWindowShell.getDisplay());
				TextTransfer textTransfer = TextTransfer.getInstance();
				clipboard.setContents(new String[] { url }, new Transfer[] { textTransfer });
				clipboard.dispose();
				////////////////////////////////////////////////////////////////////////////////////////////////////
				logger.info("复制到剪切板:" + url + (pars.size() == 0 ? ("") : ("?" + ParamUtils.mapToQuery(pars))));
				statusBar.setText("请求信息已复制到剪切板:" + url);
			}
		});
		mntmget.setText("复制GET请求URL到剪切板");

		MenuItem mntmcurl = new MenuItem(browerMenu, SWT.NONE);
		mntmcurl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (StringUtils.isEmpty(urlText.getText())) {
					return;
				}
				HashMap<String, String> pars = getParameters();
				parsText.setText(ParamUtils.mapToQuery(pars));
				StringBuilder sBuilder = new StringBuilder();
				// 接口地址
				String url = urlText.getText();
				// 开始构建curl指令
				switch (methodSelectCombo.getText()) {
				// GET请求
				case "GET":
					sBuilder.append("curl ");
					if (null != header && header.size() > 0) {
						Object[] keyArray = header.keySet().toArray();
						Arrays.sort(keyArray);
						for (Object key : keyArray) {
							if ((!header.get(key).isEmpty()) && (!header.get(key).isEmpty())) {
								sBuilder.append("-H '").append(key).append(":").append(header.get(key)).append("' ");
							}
						}
					}
					if (StringUtils.isNotEmpty(ParamUtils.mapToQuery(pars))) {
						sBuilder.append("-G -d \"").append(ParamUtils.mapToQuery(pars, true)).append("\" ");
					}
					break;
				case "POST":
					sBuilder.append("curl ");
					if (null != header && header.size() > 0) {
						Object[] keyArray = header.keySet().toArray();
						Arrays.sort(keyArray);
						for (Object key : keyArray) {
							if ((!header.get(key).isEmpty()) && (!header.get(key).isEmpty())) {
								sBuilder.append("-H '").append(key).append(":").append(header.get(key)).append("' ");
							}
						}
					}
					if (StringUtils.isNotEmpty(ParamUtils.mapToQuery(pars))) {
						sBuilder.append("-d \"").append(ParamUtils.mapToQuery(pars, true)).append("\" ");
					}
					break;
				case "HEAD":
					sBuilder.append("curl ");
					if (null != header && header.size() > 0) {
						Object[] keyArray = header.keySet().toArray();
						Arrays.sort(keyArray);
						for (Object key : keyArray) {
							if ((!header.get(key).isEmpty()) && (!header.get(key).isEmpty())) {
								sBuilder.append("-H '").append(key).append(":").append(header.get(key)).append("' ");
							}
						}
					}
					sBuilder.append("-I ");
					if (StringUtils.isNotEmpty(ParamUtils.mapToQuery(pars))) {
						sBuilder.append("-d \"").append(ParamUtils.mapToQuery(pars, true)).append("\" ");
					}
					break;
				case "PUT":
					sBuilder.append("curl ");
					if (null != header && header.size() > 0) {
						Object[] keyArray = header.keySet().toArray();
						Arrays.sort(keyArray);
						for (Object key : keyArray) {
							if ((!header.get(key).isEmpty()) && (!header.get(key).isEmpty())) {
								sBuilder.append("-H '").append(key).append(":").append(header.get(key)).append("' ");
							}
						}
					}
					sBuilder.append("-X PUT ");
					if (StringUtils.isNotEmpty(ParamUtils.mapToQuery(pars))) {
						sBuilder.append("-d \"").append(ParamUtils.mapToQuery(pars, true)).append("\" ");
					}
					break;
				case "PATCH":
					sBuilder.append("curl ");
					if (null != header && header.size() > 0) {
						Object[] keyArray = header.keySet().toArray();
						Arrays.sort(keyArray);
						for (Object key : keyArray) {
							if ((!header.get(key).isEmpty()) && (!header.get(key).isEmpty())) {
								sBuilder.append("-H '").append(key).append(":").append(header.get(key)).append("' ");
							}
						}
					}
					sBuilder.append("-X PATCH ");
					if (StringUtils.isNotEmpty(ParamUtils.mapToQuery(pars))) {
						sBuilder.append("-d \"").append(ParamUtils.mapToQuery(pars, true)).append("\" ");
					}
					break;
				case "DELETE":
					sBuilder.append("curl ");
					if (null != header && header.size() > 0) {
						Object[] keyArray = header.keySet().toArray();
						Arrays.sort(keyArray);
						for (Object key : keyArray) {
							if ((!header.get(key).isEmpty()) && (!header.get(key).isEmpty())) {
								sBuilder.append("-H '").append(key).append(":").append(header.get(key)).append("' ");
							}
						}
					}
					sBuilder.append("-X DELETE ");
					if (StringUtils.isNotEmpty(ParamUtils.mapToQuery(pars))) {
						sBuilder.append("-d \"").append(ParamUtils.mapToQuery(pars, true)).append("\" ");
					}
					break;
				default:
					break;
				}
				///////////////////////////////////////////////////////////////////////////////////////////////////
				sBuilder.append(url);
				String info = sBuilder.toString();
				Clipboard clipboard = new Clipboard(mainWindowShell.getDisplay());
				TextTransfer textTransfer = TextTransfer.getInstance();
				clipboard.setContents(new String[] { info }, new Transfer[] { textTransfer });
				clipboard.dispose();
				////////////////////////////////////////////////////////////////////////////////////////////////////
				logger.info("复制到剪切板:" + info);
				statusBar.setText("CURL指令已复制到剪切板:" + info);
			}
		});
		mntmcurl.setText("复制CURl指令到剪切板");

		// fomr参数table
		formTable = new Table(mainWindowShell, SWT.BORDER | SWT.HIDE_SELECTION | SWT.VIRTUAL);
		formTable.setBounds(3, 86, 480, 506);
		formTable.setItemCount(parsSum);
		formTable.setHeaderVisible(true);
		formTable.setLinesVisible(true);

		// 文本参数框
		reqStyledText = new StyledText(mainWindowShell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		reqStyledText.setBounds(3, 86, 480, 506);
		reqStyledText.setAlwaysShowScrollBars(true);
		formToolkit.adapt(reqStyledText);
		formToolkit.adapt(reqStyledText);
		formToolkit.paintBordersFor(reqStyledText);
		reqStyledText.setVisible(false);
		styledTextAddContextMenu(reqStyledText);

		// 表列
		TableColumn numberColumn = new TableColumn(formTable, SWT.BORDER);
		numberColumn.setWidth(38);
		numberColumn.setResizable(false);
		numberColumn.setText("编号");

		TableColumn tableColumn = new TableColumn(formTable, SWT.NONE);
		tableColumn.setResizable(false);
		tableColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				statusBar.setText("不支持非空排序");
			}
		});
		tableColumn.setWidth(38);
		tableColumn.setText("非空");

		TableColumn tipColumn = new TableColumn(formTable, SWT.BORDER);
		tipColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				statusBar.setText("不支持按备注排序");
			}
		});
		tipColumn.setWidth((int) ((formTable.getBounds().width - numberColumn.getWidth() - tableColumn.getWidth()
				- formTable.getVerticalBar().getSize().x - 4) * 0.24));
		tipColumn.setResizable(false);
		tipColumn.setText("备注");

		TableColumn nameColumn = new TableColumn(formTable, SWT.BORDER);
		nameColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				orderParameters();
			}
		});
		nameColumn.setWidth((int) ((formTable.getBounds().width - numberColumn.getWidth() - tableColumn.getWidth()
				- formTable.getVerticalBar().getSize().x - 4) * 0.38));
		nameColumn.setText("参数名");
		nameColumn.setResizable(false);

		TableColumn valueColumn = new TableColumn(formTable, SWT.BORDER);
		valueColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				statusBar.setText("不支持按参数值排序");
			}
		});
		valueColumn.setWidth((int) ((formTable.getBounds().width - numberColumn.getWidth() - tableColumn.getWidth()
				- formTable.getVerticalBar().getSize().x - 4) * 0.38));
		valueColumn.setText("参数值");
		valueColumn.setResizable(false);

		// 将Label和Text绑定到table
		label = new Label[parsSum];
		formPar = new Text[parsSum][4];
		menuItem1SubFrozen = new MenuItem[parsSum];
		TableItem[] items = formTable.getItems();
		for (int i = 0; i < parsSum; i++) {
			final int b = i;
			//////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////
			// 第一列--编号和操作列
			TableEditor editor0 = new TableEditor(formTable);
			label[i] = new Label(formTable, SWT.NONE | SWT.CENTER);
			label[i].setBackground(parBackgroundNormalColor);
			label[i].setText(new DecimalFormat("000").format(i + 1));
			editor0.grabHorizontal = true;
			editor0.setEditor(label[i], items[i], 0);

			Menu frozenPar = new Menu(label[i]);
			label[i].setMenu(frozenPar);
			menuItem1SubFrozen[i] = (MenuItem) new MenuItem(frozenPar, SWT.NONE);
			menuItem1SubFrozen[i].setText("冻结此参数");

			MenuItem mntmPubAdd = new MenuItem(frozenPar, SWT.NONE);
			mntmPubAdd.setText("添加到公共参数");
			mntmPubAdd.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					String name = formPar[b][2].getText();
					String value = formPar[b][3].getText();
					if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value)) {
						pubpar.put(name, value);
						// 保存公共参数
						apiDoc.setPublicpars(pubpar);
						// 在新的线程异步保存
						new Thread(new Runnable() {
							@Override
							public void run() {
								PubUtils.saveToFile(new File("./config/" + apiJsonFile), PubUtils.jsonFormat(
										JSON.toJSONString(apiDoc, SerializerFeature.WriteNullStringAsEmpty)));
							}
						}).start();
						logger.info("参数" + name + ":" + value + "加入到公共参数");
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			////////////////////////////////////////////////////////////////////////////////////////
			MenuItem mntmKey = new MenuItem(frozenPar, SWT.CASCADE);
			mntmKey.setText("参数名操作");

			Menu menuKey = new Menu(mntmKey);
			mntmKey.setMenu(menuKey);

			MenuItem menuItemKeyUrlEncode = new MenuItem(menuKey, SWT.NONE);
			menuItemKeyUrlEncode.setText("UrlEncode");
			menuItemKeyUrlEncode.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (StringUtils.isNotEmpty(formPar[b][2].getText())) {
						try {
							formPar[b][2].setText(URLEncoder.encode(formPar[b][2].getText(), "UTF-8"));
						} catch (UnsupportedEncodingException e1) {
							logger.debug("转码异常", e1);
						}
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			MenuItem menuItemKeyUrlDecode = new MenuItem(menuKey, SWT.NONE);
			menuItemKeyUrlDecode.setText("UrlDecode");
			menuItemKeyUrlDecode.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (StringUtils.isNotEmpty(formPar[b][2].getText())) {
						try {
							formPar[b][2].setText(URLDecoder.decode(formPar[b][2].getText(), "UTF-8"));
						} catch (UnsupportedEncodingException e1) {
							logger.debug("解码异常", e1);
						}
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			MenuItem menuItemKeyBase64 = new MenuItem(menuKey, SWT.NONE);
			menuItemKeyBase64.setText("Base64转码");
			menuItemKeyBase64.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (StringUtils.isNotEmpty(formPar[b][2].getText())) {
						formPar[b][2].setText(PubUtils.base64EncodeString(formPar[b][2].getText()));
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			MenuItem menuItemKeyBase64D = new MenuItem(menuKey, SWT.NONE);
			menuItemKeyBase64D.setText("Base64解码");
			menuItemKeyBase64D.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (StringUtils.isNotEmpty(formPar[b][2].getText())) {
						formPar[b][2].setText(PubUtils.base64DecodeString(formPar[b][2].getText()));
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			MenuItem menuItemKeyMD5Cap = new MenuItem(menuKey, SWT.NONE);
			menuItemKeyMD5Cap.setText("MD5大写");
			menuItemKeyMD5Cap.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (StringUtils.isNotEmpty(formPar[b][2].getText())) {
						formPar[b][2].setText(PubUtils.md5(formPar[b][2].getText()));
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			MenuItem menuItemKeyMD5Low = new MenuItem(menuKey, SWT.NONE);
			menuItemKeyMD5Low.setText("MD5小写");
			menuItemKeyMD5Low.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (StringUtils.isNotEmpty(formPar[b][2].getText())) {
						formPar[b][2].setText(PubUtils.md5(formPar[b][2].getText()).toLowerCase());
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			MenuItem menuItemKeyTrim = new MenuItem(menuKey, SWT.NONE);
			menuItemKeyTrim.setText("TRIM");
			menuItemKeyTrim.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (StringUtils.isNotEmpty(formPar[b][2].getText())) {
						formPar[b][2].setText(formPar[b][2].getText().trim());
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			///////////////////////////////////////////////////////////////////////////////////
			MenuItem mntmValue = new MenuItem(frozenPar, SWT.CASCADE);
			mntmValue.setText("参数值操作");

			Menu menuValue = new Menu(mntmValue);
			mntmValue.setMenu(menuValue);

			MenuItem menuItemValueUrlEncode = new MenuItem(menuValue, SWT.NONE);
			menuItemValueUrlEncode.setText("UrlEncode");
			menuItemValueUrlEncode.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (StringUtils.isNotEmpty(formPar[b][3].getText())) {
						try {
							formPar[b][3].setText(URLEncoder.encode(formPar[b][3].getText(), "UTF-8"));
						} catch (UnsupportedEncodingException e1) {
							logger.debug("转码异常", e1);
						}
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			MenuItem menuItemValueUrlDecode = new MenuItem(menuValue, SWT.NONE);
			menuItemValueUrlDecode.setText("UrlDecode");
			menuItemValueUrlDecode.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (StringUtils.isNotEmpty(formPar[b][3].getText())) {
						try {
							formPar[b][3].setText(URLDecoder.decode(formPar[b][3].getText(), "UTF-8"));
						} catch (UnsupportedEncodingException e1) {
							logger.debug("解码异常", e1);
						}
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			MenuItem menuItemValueBase64 = new MenuItem(menuValue, SWT.NONE);
			menuItemValueBase64.setText("Base64转码");
			menuItemValueBase64.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (StringUtils.isNotEmpty(formPar[b][3].getText())) {
						formPar[b][3].setText(PubUtils.base64EncodeString(formPar[b][3].getText()));
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			MenuItem menuItemValueBase64D = new MenuItem(menuValue, SWT.NONE);
			menuItemValueBase64D.setText("Base64解码");
			menuItemValueBase64D.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (StringUtils.isNotEmpty(formPar[b][3].getText())) {
						formPar[b][3].setText(PubUtils.base64DecodeString(formPar[b][3].getText()));
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			MenuItem menuItemValueMD5Cap = new MenuItem(menuValue, SWT.NONE);
			menuItemValueMD5Cap.setText("MD5大写");
			menuItemValueMD5Cap.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (StringUtils.isNotEmpty(formPar[b][3].getText())) {
						formPar[b][3].setText(PubUtils.md5(formPar[b][3].getText()));
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			MenuItem menuItemValueMD5Low = new MenuItem(menuValue, SWT.NONE);
			menuItemValueMD5Low.setText("MD5小写");
			menuItemValueMD5Low.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (StringUtils.isNotEmpty(formPar[b][3].getText())) {
						formPar[b][3].setText(PubUtils.md5(formPar[b][3].getText()).toLowerCase());
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});

			MenuItem menuItemValueTrim = new MenuItem(menuValue, SWT.NONE);
			menuItemValueTrim.setText("TRIM");
			menuItemValueTrim.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (StringUtils.isNotEmpty(formPar[b][3].getText())) {
						formPar[b][3].setText(formPar[b][3].getText().trim());
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			//////////////////////////////////////////////////////////////////////////
			// 参数冻结解冻事件
			menuItem1SubFrozen[i].addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (StringUtils.isEmpty(formPar[b][1].getText()) && StringUtils.isEmpty(formPar[b][2].getText())
							&& StringUtils.isEmpty(formPar[b][3].getText())) {
						statusBar.setText("空的输入框,放弃冻结");
					} else {
						if (StringUtils.equals(menuItem1SubFrozen[b].getText(), "冻结此参数")) {
							// 冻结标志
							label[b].setToolTipText("此参数已冻结,冻结后不再发送此参数");
							formPar[b][0].setForeground(parFontsFrozenColor);
							formPar[b][1].setForeground(parFontsFrozenColor);
							formPar[b][2].setForeground(parFontsFrozenColor);
							formPar[b][3].setForeground(parFontsFrozenColor);
							menuItem1SubFrozen[b].setText("解冻此参数");
							statusBar.setText("冻结参数完毕");
						} else if (StringUtils.equals(menuItem1SubFrozen[b].getText(), "解冻此参数")) {
							// 解冻标志
							label[b].setToolTipText("");
							formPar[b][0].setForeground(parFontsnormalColor);
							formPar[b][1].setForeground(parFontsnormalColor);
							formPar[b][2].setForeground(parFontsnormalColor);
							formPar[b][3].setForeground(parFontsnormalColor);
							menuItem1SubFrozen[b].setText("冻结此参数");
							statusBar.setText("解冻参数完毕");
						}
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e) {

				}
			});

			// 鼠标事件
			label[i].addMouseTrackListener(new MouseTrackListener() {
				@Override
				public void mouseHover(MouseEvent e) {
				}

				@Override
				public void mouseExit(MouseEvent e) {
					setParTableBackgroundNormal(b);
				}

				@Override
				public void mouseEnter(MouseEvent e) {
					setParTableBackgroundSelection(b);
				}
			});
			//////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////
			// 第2列
			TableEditor editor1 = new TableEditor(formTable);
			formPar[i][0] = new Text(formTable, SWT.NONE | SWT.CENTER);
			editor1.grabHorizontal = true;
			editor1.setEditor(formPar[i][0], items[i], 1);
			// 第3列
			TableEditor editor2 = new TableEditor(formTable);
			formPar[i][1] = new Text(formTable, SWT.NONE);
			editor2.grabHorizontal = true;
			editor2.setEditor(formPar[i][1], items[i], 2);
			// 第4列
			TableEditor editor3 = new TableEditor(formTable);
			formPar[i][2] = new Text(formTable, SWT.NONE);
			editor3.grabHorizontal = true;
			editor3.setEditor(formPar[i][2], items[i], 3);
			// 第5列
			TableEditor editor4 = new TableEditor(formTable);
			formPar[i][3] = new Text(formTable, SWT.NONE);
			editor4.grabHorizontal = true;
			editor4.setEditor(formPar[i][3], items[i], 4);
			// 设置焦点变色--鼠标和焦点
			formPar[i][0].addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					setParTableBackgroundNormal(b);
				}

				@Override
				public void focusGained(FocusEvent e) {
					setParTableBackgroundSelection(b);
				}
			});
			formPar[i][1].addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					setParTableBackgroundNormal(b);
				}

				@Override
				public void focusGained(FocusEvent e) {
					setParTableBackgroundSelection(b);
				}
			});
			formPar[i][2].addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					setParTableBackgroundNormal(b);
				}

				@Override
				public void focusGained(FocusEvent e) {
					setParTableBackgroundSelection(b);
				}
			});
			formPar[i][3].addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					setParTableBackgroundNormal(b);
				}

				@Override
				public void focusGained(FocusEvent e) {
					setParTableBackgroundSelection(b);
				}
			});
			// 鼠标监听
			formPar[i][0].addMouseTrackListener(new MouseTrackListener() {

				@Override
				public void mouseHover(MouseEvent e) {
				}

				@Override
				public void mouseExit(MouseEvent e) {
					setParTableBackgroundNormal(b);
				}

				@Override
				public void mouseEnter(MouseEvent e) {
					setParTableBackgroundSelection(b);
				}
			});

			formPar[i][1].addMouseTrackListener(new MouseTrackListener() {

				@Override
				public void mouseHover(MouseEvent e) {
				}

				@Override
				public void mouseExit(MouseEvent e) {
					setParTableBackgroundNormal(b);
				}

				@Override
				public void mouseEnter(MouseEvent e) {
					setParTableBackgroundSelection(b);
				}
			});

			formPar[i][2].addMouseTrackListener(new MouseTrackListener() {

				@Override
				public void mouseHover(MouseEvent e) {
				}

				@Override
				public void mouseExit(MouseEvent e) {
					setParTableBackgroundNormal(b);
				}

				@Override
				public void mouseEnter(MouseEvent e) {
					setParTableBackgroundSelection(b);
				}
			});

			formPar[i][3].addMouseTrackListener(new MouseTrackListener() {

				@Override
				public void mouseHover(MouseEvent e) {
				}

				@Override
				public void mouseExit(MouseEvent e) {
					setParTableBackgroundNormal(b);
				}

				@Override
				public void mouseEnter(MouseEvent e) {
					setParTableBackgroundSelection(b);
				}
			});
		}

		// 接口返回内容显示区域
		cTabFolder = new CTabFolder(mainWindowShell, SWT.BORDER | SWT.FLAT);
		cTabFolder.setBounds(487, 62, 649, 530);
		cTabFolder.setSelectionBackground(
				Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		cTabFolder.forceFocus();
		CTabItem tabItem = new CTabItem(cTabFolder, SWT.BORDER);
		tabItem.setText(" 响应内容  ");
		CTabItem tabItem2 = new CTabItem(cTabFolder, SWT.BORDER);
		tabItem2.setText(" 响应头部 ");

		resultBodyStyledText = new StyledText(cTabFolder, SWT.NONE | SWT.V_SCROLL | SWT.H_SCROLL);
		tabItem.setControl(resultBodyStyledText);
		resultBodyStyledText.setAlwaysShowScrollBars(true);
		formToolkit.adapt(resultBodyStyledText);
		styledTextAddContextMenu(resultBodyStyledText);

		resultHeaderStyledText = new StyledText(cTabFolder, SWT.NONE | SWT.V_SCROLL | SWT.H_SCROLL);
		tabItem2.setControl(resultHeaderStyledText);
		resultHeaderStyledText.setAlwaysShowScrollBars(true);
		formToolkit.adapt(resultHeaderStyledText);
		styledTextAddContextMenu(resultHeaderStyledText);

		// 状态栏
		statusBar = new Text(mainWindowShell, SWT.BORDER);
		statusBar.setBounds(3, 595, 1134, 25);
		formToolkit.adapt(statusBar, true, true);

		// 各个组件的监听事件//////////////////////////////////////////////////////////////////////////////////////////
		// 全局快捷键--要注意阻止快捷键重复执行
		// 按键按下时执行快捷键操作
		shortcutListener = new Listener() {
			public void handleEvent(Event e) {
				// 只有窗口是激活状态,并且按键是第一次按下时才执行快捷键操作,避免重复不停的执行
				if ((!openByShortcutFlag) && windowFocusFlag && (!keyDownFlag)) {
					// Ctrl+n开启新的窗口
					if ((e.stateMask == SWT.CTRL) && (e.keyCode == KeyCode.KEY_N)) {
						keyDownFlag = true;
						initNewWindow(false, true);
					}
					// Ctrl+Q执行提交
					if ((e.stateMask == SWT.CTRL) && (e.keyCode == KeyCode.KEY_Q)) {
						keyDownFlag = true;
						sentRequest();
					}
					// Ctrl+Enter执行提交
					if ((e.stateMask == SWT.CTRL)
							&& ((e.keyCode == KeyCode.ENTER) || (e.keyCode == KeyCode.SMALL_KEY_BOARD_ENTER))) {
						keyDownFlag = true;
						sentRequest();
					}
					// Ctrl+l清空结果
					if ((e.stateMask == SWT.CTRL) && (e.keyCode == KeyCode.KEY_L)) {
						keyDownFlag = true;
						clearResult();
					}
					// Ctrl+s保存参数到文件
					if ((e.stateMask == SWT.CTRL) && (e.keyCode == KeyCode.KEY_S)) {
						keyDownFlag = true;
						savePars2Memory();
						savePars2File();
					}
					// Ctrl+F搜索
					if ((e.stateMask == SWT.CTRL) && (e.keyCode == KeyCode.KEY_F)) {
						keyDownFlag = true;
						if (resultBodyStyledText.isFocusControl()) {
							searchText(resultBodyStyledText);
						}
						if (resultHeaderStyledText.isFocusControl()) {
							searchText(resultHeaderStyledText);
						}
					}
				}
			}
		};
		// 按键释放时标记
		shortcutListenerRecover = new Listener() {
			@Override
			public void handleEvent(Event event) {
				openByShortcutFlag = false;
				keyDownFlag = false;
			}
		};
		// 窗口活动标志
		mainWindowShell.addListener(SWT.Activate, new Listener() {
			@Override
			public void handleEvent(Event event) {
				windowFocusFlag = true;
				logger.debug(mainWindowShell.hashCode() + "窗口获得焦点");
			}
		});
		mainWindowShell.addListener(SWT.Deactivate, new Listener() {
			@Override
			public void handleEvent(Event event) {
				windowFocusFlag = false;
				logger.debug(mainWindowShell.hashCode() + "窗口失去焦点");
			}
		});
		display.addFilter(SWT.KeyDown, shortcutListener);
		display.addFilter(SWT.KeyUp, shortcutListenerRecover);

		// 保存事件
		menuItemSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				savePars2Memory();
			}
		});
		// 保存事件
		menuItemSaveToFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				savePars2Memory();
				savePars2File();
			}
		});
		// 参数重排
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				orderParameters();
			}
		});

		// 关于-点击事件
		menuItemAbout.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AboutTools about = new AboutTools(mainWindowShell, SWT.CLOSE | SWT.SYSTEM_MODAL);
				about.open(Resource.APIEXPLAIN, Resource.APIVERSION);
			}
		});

		// 手册
		menuItemManual.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch(Resource.MANUAL);
			}
		});

		// 问题反馈
		menuItemFeedBack.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Program.launch(Resource.FEEDBACK);
			}
		});

		// 重置参数事件
		parsClearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 在别的ToolTipText更新时,鼠标点击所在的Button的ToolTipText会不停地闪烁,需要纠正
				parsClearButton.setToolTipText(null);
				try {
					urlText.setText(serverAdress + apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem()
							.get(interfaceCombo.getSelectionIndex()).getPath());
					initParameters(apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem()
							.get(interfaceCombo.getSelectionIndex()).getParameters());
					// 重置参数时,删除内存中临时保存的数据
					tempSavePars.remove(apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem()
							.get(interfaceCombo.getSelectionIndex()).getUuid());
				} catch (Exception e2) {
					logger.error("当前选择的接口并不包含参数信息,无法完成重新初始化,默认留空");
				}
			}
		});
		parsClearButton.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseExit(MouseEvent e) {
				parsClearButton.setToolTipText("重置参数为接口文档中定义的参数");
			}
		});
		// 清空空格点击事件
		clearSpaceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 遍历参数框,找到参数里多余的空格
				for (int i = 0; i < parsSum; i++) {
					formPar[i][0].setText(formPar[i][0].getText().trim());
					formPar[i][1].setText(formPar[i][1].getText().trim());
					formPar[i][2].setText(formPar[i][2].getText().trim());
					formPar[i][3].setText(formPar[i][3].getText().trim());
				}
				logger.debug("寻找参数里的多余的空格完毕");
			}
		});

		// 分组选择事件
		modSelectCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				initSelectMod(modSelectCombo.getSelectionIndex());
				logger.debug("切换到分组:" + apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getName());
			}
		});

		// 接口选择事件
		interfaceCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				initSelectInterface(modSelectCombo.getSelectionIndex(), interfaceCombo.getSelectionIndex());
			}
		});

		// 切换表单发送方式的点击事件
		methodSelectCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				logger.debug("切换表单发送方式为:" + methodSelectCombo.getText());
				// 配置可选的ContentType
				switch (methodSelectCombo.getSelectionIndex()) {
				case 0:
					contentTypeNull.setEnabled(true);
					contentTypexwwwForm.setEnabled(false);
					contentTypeJson.setEnabled(false);
					contentTypeJavaScript.setEnabled(false);
					contentTypeApplicationXml.setEnabled(false);
					contentTypeTextPlain.setEnabled(false);
					contentTypeTextXml.setEnabled(false);
					contentTypeTextHtml.setEnabled(false);
					header.remove("Content-Type");
					reqStyledText.setVisible(false);
					formTable.setVisible(true);
					break;
				case 2:
					contentTypeNull.setEnabled(true);
					contentTypexwwwForm.setEnabled(false);
					contentTypeJson.setEnabled(false);
					contentTypeJavaScript.setEnabled(false);
					contentTypeApplicationXml.setEnabled(false);
					contentTypeTextPlain.setEnabled(false);
					contentTypeTextXml.setEnabled(false);
					contentTypeTextHtml.setEnabled(false);
					header.remove("Content-Type");
					reqStyledText.setVisible(false);
					formTable.setVisible(true);
				default:
					contentTypeNull.setEnabled(true);
					contentTypexwwwForm.setEnabled(true);
					contentTypeJson.setEnabled(true);
					contentTypeJavaScript.setEnabled(true);
					contentTypeApplicationXml.setEnabled(true);
					contentTypeTextPlain.setEnabled(true);
					contentTypeTextXml.setEnabled(true);
					contentTypeTextHtml.setEnabled(true);
					header.remove("Content-Type");
					reqStyledText.setVisible(false);
					formTable.setVisible(true);
					break;
				}
			}
		});

		// 在浏览器中打开的按钮事件
		toBrower.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (StringUtils.isEmpty(urlText.getText())) {
					statusBar.setText("空地址无法发起请求");
					return;
				}
				HashMap<String, String> pars = getParameters();
				parsText.setText(ParamUtils.mapToQuery(pars));
				String url = urlText.getText();
				Program.launch(url + (pars.size() == 0 ? ("") : ("?" + ParamUtils.mapToQuery(pars))));
				logger.info("浏览器中打开:" + url + (pars.size() == 0 ? ("") : ("?" + ParamUtils.mapToQuery(pars))));
				statusBar.setText("已在浏览器中发起请求");
			}
		});

		// 此为为提交按钮添加点击事件
		submitButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				sentRequest();
			}
		});

		// 此为导入参数按钮添加点击事件
		parsCovertButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 需要尝试url解码
				String queryString;
				try {
					queryString = URLDecoder.decode(parsText.getText(), "UTF-8");
					if (queryString.equals("")) {
						logger.info("参数串为空,停止导入");
						statusBar.setText("参数串为空,停止导入");
						return;
					}
					HashMap<String, String> queryMap = new HashMap<String, String>();
					queryMap = ParamUtils.queryToMap(queryString);
					// 新的参数覆盖方案，遍历原参数，如果原参数存在则覆盖value值，否则添加新的参数
					// 第一遍遍历，覆盖，将存在的参数值替换为参数串里的
					for (int i = 0; i < formPar.length; i++) {
						String value = queryMap.get(formPar[i][2].getText());
						if (StringUtils.isNotEmpty(value) && StringUtils.isNotEmpty(formPar[i][2].getText())) {
							formPar[i][3].setText(value);
							// 将使用过的参数删除
							queryMap.remove(formPar[i][2].getText());
						}
					}
					// 第二次遍历，如果map里还有未使用的参数，则追加到原参数下面
					if (!queryMap.isEmpty()) {
						ArrayList<ApiPar> apiPars = covertHashMaptoApiPar(queryMap);
						int index = 0;
						// 寻找可以插入参数的空闲位置，逐个插入
						for (int i = 0; i < formPar.length; i++) {
							if (index == apiPars.size()) {
								// 如果所有参数都已经填充完毕则跳出循环
								break;
							}
							if (StringUtils.isEmpty(formPar[i][1].getText())
									&& StringUtils.isEmpty(formPar[i][2].getText())
									&& StringUtils.isEmpty(formPar[i][1].getText())) {
								formPar[i][1].setText("");
								formPar[i][1].setToolTipText("");
								formPar[i][2].setText(apiPars.get(index).getName());
								formPar[i][3].setText(apiPars.get(index).getValue());
								index++;
							}
						}
						// 判断是否存在参数框比剩下的参数多的问题
						if (index < apiPars.size()) {
							statusBar.setText("由于导入的参数过多，存在未导入的参数");
						}
					}
				} catch (UnsupportedEncodingException e1) {
					logger.error("异常", e1);
				}
			}
		});

		// 清除内容
		textClearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				clearResult();
			}
		});
	}

	// 文本内容搜索
	protected void searchText(StyledText resultBodyStyledText) {
		if (null == textSearch || null == textSearch.getshell() || textSearch.getshell().isDisposed()) {
			textSearch = new TextSearch(mainWindowShell, SWT.CLOSE);
			textSearch.open(resultBodyStyledText);
		} else {
			// 如果搜索框还在,直接使用
			textSearch.updateTextAndActiveWindow(resultBodyStyledText);
		}
	}

	// 提交请求
	private void sentRequest() {
		if (StringUtils.isEmpty(urlText.getText())) {
			statusBar.setText("地址为空");
			return;
		}
		// 获取请求要素
		final HashMap<String, String> pars = getParameters();
		final String url = urlText.getText();
		final String method = methodSelectCombo.getText();
		// 更新界面显示
		resultBodyStyledText.setText("");
		resultHeaderStyledText.setText("");
		statusBar.setText("请求中······");
		parsText.setText(ParamUtils.mapToQuery(pars));
		// 开始发起请求
		Thread httpThread = new Thread() {
			public void run() {
				logger.debug("请求方法:" + method);
				logger.debug("请求信息:" + url + "?" + ParamUtils.mapToQuery(pars));
				final long sumbegintime = System.currentTimeMillis();
				RawResponse result = null;
				try {
					switch (method) {
					case "GET":
						result = PubUtils.httpGet(url, pars, header, cookies, Charset.forName(settingReqCharSet));
						break;
					case "POST":
						result = PubUtils.httpPost(url, pars, header, cookies, Charset.forName(settingReqCharSet));
						break;
					case "HEAD":
						result = PubUtils.httpHead(url, pars, header, cookies, Charset.forName(settingReqCharSet));
						break;
					case "PUT":
						result = PubUtils.httpPost(url, pars, header, cookies, Charset.forName(settingReqCharSet));
						break;
					case "PATCH":
						result = PubUtils.httpPatch(url, pars, header, cookies, Charset.forName(settingReqCharSet));
						break;
					case "DELETE":
						result = PubUtils.httpDelete(url, pars, header, cookies, Charset.forName(settingReqCharSet));
						break;
					default:
						logger.debug("HTTP请求时未找到可用的方法");
						break;
					}
					// 获取http请求时间
					httpTime = System.currentTimeMillis() - sumbegintime;
					// 响应正文byte数组
					resultByte = result.readToBytes();
					// 获取httpcode
					httpCode = result.getStatusCode();
					// 获取头部信息和编码
					headerReturnStr = "";
					autoCheckResCharSet = null;
					List<Parameter<String>> header = result.getHeaders();
					for (int i = 0; i < header.size(); i++) {
						headerReturnStr += header.get(i).getKey() + ":" + header.get(i).getValue() + "\n";
						// 自动检测编码
						if (header.get(i).getKey().toUpperCase().equals("CONTENT-TYPE")) {
							logger.debug("开始寻找编码信息");
							autoCheckResCharSet = header.get(i).getValue()
									.substring(header.get(i).getValue().toUpperCase().indexOf("CHARSET=") + 8);
							logger.debug("从Response Header中读取到编码格式:" + autoCheckResCharSet);
						}
					}
					// 这里后期要添加更丰富的返回类型判断,比如侦测返回的是图像则显示图像等
					if (true) {
						// 使用指定编码解码
						if (StringUtils.equals(settingResCharSet, "auto")) {
							bodyReturnStr = decodeString(resultByte, autoCheckResCharSet);
						} else {
							bodyReturnStr = decodeString(resultByte, settingResCharSet);
						}
						display.syncExec(new Thread() {
							public void run() {
								resultBodyStyledText
										.setText(StringUtils.isNotEmpty(bodyReturnStr) ? bodyReturnStr : "");
								resultHeaderStyledText
										.setText(StringUtils.isNotEmpty(headerReturnStr) ? headerReturnStr : "");
								statusBar.setText("请求结束/HTTP状态码:" + httpCode + "/HTTP请求耗时:" + httpTime + "ms" + "/总耗时:"
										+ (System.currentTimeMillis() - sumbegintime) + "ms");
							}
						});
					}
				} catch (final Exception e) {
					logger.error("异常", e);
					display.syncExec(new Thread() {
						public void run() {
							statusBar.setText("HTTP请求异常,请重试,异常信息为:" + e.toString());
						}
					});
				}
			}
		};
		httpThread.setName("httpRequest");
		httpThread.start();

	}

	// 编码设置
	// 按照指定的编码解码字符串,如果传入的编码方式不在列表支持的范围内，
	// 则尝试自动编码(如果http响应结果中获取自动编码失败则使用系统默认方式编码)，
	// 尝试自动编码失败后则使用系统默认编码方式编码
	private String decodeString(byte[] bytes, String charSet) {
		logger.debug("传入的编码方式为:" + charSet);
		String string = null;
		try {
			switch (charSet.toUpperCase()) {
			case "UTF-8":
				string = new String(bytes, "UTF-8");
				break;
			case "GBK":
				string = new String(bytes, "GBK");
				break;
			case "GB2312":
				string = new String(bytes, "GB2312");
				break;
			case "GB18030":
				string = new String(bytes, "GB18030");
				break;
			case "BIG5":
				string = new String(bytes, "Big5");
				break;
			case "BIG5-HKSCS":
				string = new String(bytes, "Big5-HKSCS");
				break;
			case "ISO-8859-1":
				string = new String(bytes, "ISO-8859-1");
				break;
			default:
				if (StringUtils.isNotEmpty(autoCheckResCharSet)) {
					string = new String(bytes, autoCheckResCharSet);
				} else {
					string = new String(bytes);
				}
				break;
			}
		} catch (Exception e) {
			logger.debug("异常", e);
			logger.debug("未找到可用的编码方式，且自动编码失败，使用系统默认编码方式进行编码");
			string = new String(bytes);
		}
		return PubUtils.jsonFormat(string);
	}

	// 按照指定的编码更新resultBodyStyledText
	private void updateResultBodyStyledText() {
		if (null == resultByte) {
			return;
		}
		// 异步解码
		new Thread() {
			public void run() {
				if (StringUtils.equals(settingResCharSet, "auto")) {
					final String string = decodeString(resultByte, autoCheckResCharSet);
					display.syncExec(new Thread() {
						public void run() {
							resultBodyStyledText.setText(string);
						}
					});
				} else {
					final String string = decodeString(resultByte, settingResCharSet);
					display.syncExec(new Thread() {
						public void run() {
							resultBodyStyledText.setText(string);
						}
					});
				}

			}
		}.start();
	}

	// 保存参数到内存
	private void savePars2Memory() {
		logger.debug("调用了临时保存参数");
		if (modSelectCombo.getSelectionIndex() == -1 | interfaceCombo.getSelectionIndex() == -1) {
			statusBar.setText("保存失败：只允许在现有接口上保存已填写的参数");
			return;
		}
		// 获取当前文档节点
		ApiItem item = tempSavePars.get(apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem()
				.get(interfaceCombo.getSelectionIndex()).getUuid());
		if (null == item) {
			item = new ApiItem();
		}
		item.setUuid(apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem()
				.get(interfaceCombo.getSelectionIndex()).getUuid());
		item.setName(interfaceCombo.getText());
		item.setDescription(interfaceCombo.getToolTipText());
		item.setPath(urlText.getText().replace(serverAdress, ""));
		item.setMethod(methodSelectCombo.getText());
		item.setParameters(new ArrayList<ApiPar>());
		// 从form框初始化
		for (int i = 0; i < formPar.length; i++) {
			// 判断参数名和不判断备注,参数值
			if (StringUtils.isNotEmpty(formPar[i][2].getText())) {
				item.getParameters().add(new ApiPar(formPar[i][2].getText() + "", formPar[i][1].getText() + "",
						formPar[i][3].getText() + "", StringUtils.equals(formPar[i][2].getText(), "Y") ? false : true));
			}
		}
		// 做个标识临时存起来
		tempSavePars.put(apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem()
				.get(interfaceCombo.getSelectionIndex()).getUuid(), item);
		statusBar.setText("保存成功,程序关闭前有效");
	}

	// 保存参数到文件
	// 新读取一份文档保存,因为内存中的那份可能在其他接口处做了临时保存
	private void savePars2File() {
		logger.debug("调用了永久保存参数");
		if (modSelectCombo.getSelectionIndex() == -1 | interfaceCombo.getSelectionIndex() == -1) {
			return;
		}
		try {
			// 将临时区的内容拷贝到加载的文档里
			apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem().set(interfaceCombo.getSelectionIndex(),
					tempSavePars.get(apiDoc.getItem().get(modSelectCombo.getSelectionIndex()).getItem()
							.get(interfaceCombo.getSelectionIndex()).getUuid()));
			// 保存到文件--潜在的风险,保存时间过长程序界面卡死
			if (PubUtils.saveToFile(new File("./config/" + apiJsonFile),
					PubUtils.jsonFormat(JSON.toJSONString(apiDoc, SerializerFeature.WriteNullStringAsEmpty)))) {
				statusBar.setText("保存成功,已写入接口配置文件");
			} else {
				statusBar.setText("保存失败,请重试");
			}
		} catch (Exception e) {
			logger.error("异常", e);
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// 初始化程序
	private void initSystem() {
		// 加载并初始化参数信息
		methodSelectCombo.select(0);
		logger.debug("初始化配置信息-默认表单发送方式为:" + this.methodSelectCombo.getText());
		File file = new File("./config");
		if (!file.exists()) {
			file.mkdir();
			logger.warn("警告:初始化配置信息出错,配置目录不存在,已创建");
		} else if (!file.isDirectory()) {
			logger.warn("警告:初始化配置信息出错,配置目录不存在,已创建");
			file.delete();
			file.mkdir();
		}
		File file1 = new File("./log");
		if (!file1.exists()) {
			file1.mkdir();
			logger.warn("警告:日志目录不存在,已创建");
		} else if (!file1.isDirectory()) {
			logger.warn("警告:日志目录不存在,已创建");
			file1.delete();
			file1.mkdir();
		}
		// 读取并加载配置文件
		File configFile = new File("./config/config.properties");
		if (!configFile.exists()) {
			try {
				PubUtils.saveToFile(configFile, Resource.CONFIG);
				logger.warn("警告:参数配置文件丢失,已创建默认配置");
				// 当创建默认配置文档的时候也生成个默认的接口列表--心知天气
				/////////////////////////// 示例接口//////////////////////////////////////
				PubUtils.saveToFile(new File("./config/api-xinzhiweather.json"), PubUtils.jsonFormat(
						JSON.toJSONString(new XinzhiWeather().getApidoc(), SerializerFeature.WriteNullStringAsEmpty)));
			} catch (Exception e) {
				logger.warn("异常", e);
			}
		}
		// 此处开始加载配置文件内容
		try {
			// 加载配置
			properties = PubUtils.readProperties(configFile);
			// 加载API列表
			loadApiJsonFileArray = properties.getProperty("apilist").split("\\|");
			if (null != loadApiJsonFileArray && loadApiJsonFileArray.length > 0) {
				if (StringUtils.isNotEmpty(loadApiJsonFileArray[0])) {
					this.apiJsonFile = loadApiJsonFileArray[0];
					// 初始化API下拉选择框
					for (int i = 0; i < loadApiJsonFileArray.length; i++) {
						final MenuItem apiItem = new MenuItem(apis, SWT.NONE);
						apiItem.setText(loadApiJsonFileArray[i]);
						if (i == 0) {
							apiItem.setImage(SWTResourceManager.getImage(MainWindow.class, Resource.IMAGE_CHECKED));
						}
						apiItem.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								// 设置焦点
								for (int i = 0; i < apis.getItemCount(); i++) {
									apis.getItem(i).setImage(null);
								}
								apiItem.setImage(SWTResourceManager.getImage(MainWindow.class, Resource.IMAGE_CHECKED));
								apiJsonFile = apiItem.getText();
								initApiList();
							}
						});
					}
				} else {
					this.apiJsonFile = null;
				}
			}
		} catch (Exception e) {
			statusBar.setText("读取配置失败,请检查");
			logger.warn("读取配置失败,请检查", e);
		}
		// 配置文件加载完毕后开始加载API列表
		if (null == apiJsonFile) {
			logger.debug("API列表为空,跳过加载");
		} else {
			initApiList();
		}
	}

	// 初始化API列表信息
	private void initApiList() {
		File apilistfile = new File("./config/" + apiJsonFile);
		if (!apilistfile.exists()) {
			logger.warn("警告:您加载的API文档不存在,程序将跳过加载API列表,请检查配置");
			return;
		}
		try {
			apiDoc = JSON.parseObject(PubUtils.readFromFile(apilistfile, "UTF-8"), ApiDoc.class);
			// 检查接口文档是老版本的文档没有,需要补正
			// 加载前判断版本
			if (apiDoc.getDecodeversion().equals(1.1)) {
				logger.debug("加载的api版本为" + apiDoc.getVersion());
				initServerList(apiDoc.getServerlist());
				if (null != apiDoc.getPublicpars()) {
					pubpar = apiDoc.getPublicpars();
				}
				if (null != apiDoc.getItem() | apiDoc.getItem().size() > 0) {
					initMod();
				} else {
					logger.debug("空的接口文档,跳过初始化接口列表");
					clearParameters();
				}
			} else {
				logger.warn("警告:您加载的API列表是不兼容的版本,请重新生成接口文档");
				statusBar.setText("警告:您加载的API列表是不兼容的版本,请重新生成接口文档");
			}
		} catch (Exception e) {
			logger.error("异常:", e);
			statusBar.setText("您加载的接口文件有误,请核对");
		}
	}

	// 地址列表加载方法
	private void initServerList(String serverlist) {
		// 移除之前的数据
		for (MenuItem menuItem : servers.getItems()) {
			menuItem.dispose();
		}
		// 加载地址列表
		if (null == serverlist) {
			logger.info("没有读取到服务器信息,调过加载");
			return;
		}
		logger.info("加载到的服务器列表:" + serverlist);
		loadServerAdressArray = serverlist.split("\\|");
		if (null != loadServerAdressArray && loadServerAdressArray.length > 0) {
			if (StringUtils.isNotEmpty(loadServerAdressArray[0])) {
				this.serverAdress = loadServerAdressArray[0];
				// 初始化服务器下拉选择框
				for (int i = 0; i < loadServerAdressArray.length; i++) {
					final MenuItem serverItem = new MenuItem(servers, SWT.NONE);
					serverItem.setText(loadServerAdressArray[i]);
					if (i == 0) {
						serverItem.setImage(SWTResourceManager.getImage(MainWindow.class, Resource.IMAGE_CHECKED));
					}
					serverItem.addSelectionListener(new SelectionAdapter() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							// 设置焦点
							for (int i = 0; i < servers.getItemCount(); i++) {
								servers.getItem(i).setImage(null);
							}
							serverItem.setImage(SWTResourceManager.getImage(MainWindow.class, Resource.IMAGE_CHECKED));
							serverAdress = serverItem.getText();
							urlText.setText(serverAdress + interfaceContextPath);
						}
					});
				}
			} else {
				this.serverAdress = "";
			}
		}
	}

	// 初始化接口模块
	private void initMod() {
		modSelectCombo.removeAll();
		interfaceCombo.removeAll();
		if (null == apiDoc.getItem() | apiDoc.getItem().size() == 0) {
			clearParameters();
			return;
		}
		for (int i = 0; i < apiDoc.getItem().size(); i++) {
			modSelectCombo.add(apiDoc.getItem().get(i).getName());
			logger.debug("API分类:" + apiDoc.getItem().get(i).getName() + "加载完毕");
		}
		if (modSelectCombo.getItemCount() > 0) {
			modSelectCombo.select(0);
			initSelectMod(0);
		}
	}

	// 初始化选择的模块
	private void initSelectMod(int modindex) {
		if (StringUtils.isNotEmpty(apiDoc.getItem().get(modindex).getDescription())) {
			modSelectCombo.setToolTipText(apiDoc.getItem().get(modindex).getDescription());
		} else {
			modSelectCombo.setToolTipText("");
		}
		interfaceCombo.removeAll();
		if (null == apiDoc.getItem().get(modindex).getItem() || apiDoc.getItem().get(modindex).getItem().size() == 0) {
			// 如果此模块下没有接口,则不再加载接口信息
			logger.debug("当前分类下无接口信息,跳过加载");
			mainWindowShell.setText(applicationName);
			clearParameters();
			interfaceContextPath = "";
			urlText.setText(serverAdress);
			return;
		}
		for (int i = 0; i < apiDoc.getItem().get(modindex).getItem().size(); i++) {
			interfaceCombo.add(apiDoc.getItem().get(modindex).getItem().get(i).getName());
		}
		try {
			// 默认初始化这个分类下的第一个接口
			interfaceCombo.select(0);
			initSelectInterface(modindex, interfaceCombo.getSelectionIndex());

		} catch (Exception e) {
			logger.error("异常", e);
			urlText.setText("");
		}
	}

	// 初始化选择的接口
	private void initSelectInterface(int modindex, int interfaceindex) {
		// 初始化前要判断是否之前有保存过,如果有保存过,则初始化保存的那份数据
		interfaceContextPath = "";
		ApiItem apiItem = tempSavePars.get(apiDoc.getItem().get(modindex).getItem().get(interfaceindex).getUuid());
		if (null != apiItem) {
			logger.debug("此接口有之前保存的数据,读取保存的数据");
			interfaceContextPath = apiItem.getPath();
			urlText.setText(serverAdress + apiItem.getPath());
			interfaceCombo.setToolTipText(apiItem.getDescription());
			mainWindowShell.setText(applicationName + "-" + interfaceCombo.getText());
			methodChoice(apiItem.getMethod());
			initParameters(apiItem.getParameters());
			logger.debug("切换到接口:" + apiDoc.getItem().get(modindex).getItem().get(interfaceindex).getName());
		} else {
			interfaceContextPath = apiDoc.getItem().get(modindex).getItem().get(interfaceindex).getPath();
			urlText.setText(serverAdress + interfaceContextPath);
			interfaceCombo
					.setToolTipText(apiDoc.getItem().get(modindex).getItem().get(interfaceindex).getDescription());
			mainWindowShell.setText(applicationName + "-" + interfaceCombo.getText());
			methodChoice(apiDoc.getItem().get(modindex).getItem().get(interfaceindex).getMethod());
			initParameters(apiDoc.getItem().get(modindex).getItem().get(interfaceindex).getParameters());
			logger.debug("切换到接口:" + apiDoc.getItem().get(modindex).getItem().get(interfaceindex).getName());
		}
	}

	// 参数初始化
	private void initParameters(ArrayList<ApiPar> pars) {
		orderFlag = 0;
		clearParameters();
		if (null != pars) {
			for (int i = 0; i < pars.size(); i++) {
				if (i > (this.parsSum - 1)) {
					logger.info("使用的参数超过了" + parsSum + "个");
					statusBar.setText("暂不支持" + parsSum + "个以上参数");
					break;
				}
				// 将参数初始化一下
				formPar[i][1].setText(pars.get(i).getTip() + "");
				formPar[i][2].setText(pars.get(i).getName() + "");
				formPar[i][3].setText(pars.get(i).getValue() + "");
				// 非空标记
				if (pars.get(i).isIsnull()) {
					formPar[i][0].setText("N");
				} else {
					formPar[i][0].setText("Y");
				}
			}
		}
		// 初始化公共参数
		initPubParameters(pubpar);
	}

	// 公共参数初始化
	private void initPubParameters(HashMap<String, String> parssrc) {
		// 深度拷贝
		@SuppressWarnings("unchecked")
		LinkedHashMap<String, String> pars = JSON.parseObject(JSON.toJSONString(parssrc),
				new LinkedHashMap<String, JSONObject>().getClass());
		if (null != pars) {
			for (int i = 0; i < parsSum; i++) {
				String parname = formPar[i][2].getText();
				if ((null != parname) && (null != pars.get(parname))) {
					formPar[i][3].setText(pars.get(parname));
					// 使用后移除
					logger.debug("从临时变量里移除公共参数:" + parname);
					pars.remove(parname);
				}
			}
			// 如果还有剩余公共参数，则补充填写到未使用的参数框
			if (null != pars && pars.size() > 0) {
				// 转换为list，方便按照索引位置读取
				ArrayList<ApiPar> list = covertHashMaptoApiPar(pars);
				int j = 0;
				for (int i = 0; i < parsSum; i++) {
					String parname = formPar[i][2].getText();
					String parvalue = formPar[i][3].getText();
					if (StringUtils.isEmpty(parname) && StringUtils.isEmpty(parvalue)) {
						formPar[i][2].setText(list.get(j).getName());
						formPar[i][3].setText(list.get(j).getValue());
						j++;
						// 没有更多可用公共参数了
						if (j > list.size() - 1) {
							break;
						}
						// 都初始化到最后一个了，公共参数还没初始化完，则提示
						if (i == parsSum - 1) {
							statusBar.setText("有太多公共参数要初始化，参数框不够了...");
						}
					}
				}
			}
		}
	}

	// 请求方法选择器
	public void methodChoice(String method) {
		if (null != method) {
			switch (method.toUpperCase()) {
			case "GET":
				methodSelectCombo.select(0);
				break;
			case "POST":
				methodSelectCombo.select(1);
				break;
			case "HEAD":
				methodSelectCombo.select(2);
				break;
			case "PUT":
				methodSelectCombo.select(3);
				break;
			case "PATCH":
				methodSelectCombo.select(4);
				break;
			case "DELETE":
				methodSelectCombo.select(5);
				break;
			default:
				logger.info("未找到合适的请求方法,默认使用GET");
				methodSelectCombo.select(0);
				break;
			}
		}
	}

	// 转换HashMap到ArrayList<ApiPar>
	private ArrayList<ApiPar> covertHashMaptoApiPar(HashMap<String, String> queryMap) {
		if (null == queryMap) {
			return null;
		}
		ArrayList<ApiPar> apiPars = new ArrayList<ApiPar>();
		for (Entry<String, String> entry : queryMap.entrySet()) {
			apiPars.add(new ApiPar(entry.getKey(), "", entry.getValue()));
		}
		return apiPars;
	}

	// 获取输入框中的参数-供发起请求的时候使用--仅未冻结的参数
	private HashMap<String, String> getParameters() {
		HashMap<String, String> par = new HashMap<String, String>();
		for (int i = 0; i < parsSum; i++) {
			if (!(formPar[i][2].getText().isEmpty() || formPar[i][3].getText().isEmpty())
					&& !(formPar[i][2].getForeground().equals(parFontsFrozenColor))) {
				par.put(formPar[i][2].getText(), formPar[i][3].getText());
			}
		}
		return par;
	}

	// 清空结果
	private void clearResult() {
		resultBodyStyledText.setText("");
		resultHeaderStyledText.setText("");
		statusBar.setText("");
		logger.debug("清理结束");
	}

	// 清空参数信息-清空表单和参数输入框
	private void clearParameters() {
		statusBar.setText("");
		parsText.setText("");
		for (int i = 0; i < parsSum; i++) {
			label[i].setToolTipText("");
			menuItem1SubFrozen[i].setText("冻结此参数");
			formPar[i][0].setText("");
			formPar[i][1].setText("");
			formPar[i][1].setToolTipText("");
			formPar[i][2].setText("");
			formPar[i][3].setText("");
			formPar[i][1].setForeground(parFontsnormalColor);
			formPar[i][2].setForeground(parFontsnormalColor);
			formPar[i][3].setForeground(parFontsnormalColor);
		}
	}

	// 参数重排
	@SuppressWarnings("unchecked")
	private void orderParameters() {
		// 获取所有参数
		ArrayList<ApiPar2> orderPars = new ArrayList<>();
		for (int i = 0; i < parsSum; i++) {
			if (!formPar[i][1].getText().trim().isEmpty() || !formPar[i][2].getText().trim().isEmpty()
					|| !formPar[i][3].getText().trim().isEmpty()) {
				ApiPar2 parInfo = new ApiPar2(formPar[i][2].getText(), formPar[i][1].getText(), formPar[i][3].getText(),
						(StringUtils.equals(formPar[i][0].getText(), "Y") ? false : true),
						formPar[i][1].getForeground().equals(parFontsFrozenColor));
				orderPars.add(parInfo);
				// 获取参数后清除输入框内容
				menuItem1SubFrozen[i].setText("冻结此参数");
				label[i].setToolTipText("");
				formPar[i][0].setText("");
				formPar[i][1].setText("");
				formPar[i][1].setToolTipText("");
				formPar[i][2].setText("");
				formPar[i][3].setText("");
				formPar[i][1].setForeground(parFontsnormalColor);
				formPar[i][2].setForeground(parFontsnormalColor);
				formPar[i][3].setForeground(parFontsnormalColor);
			}
		}
		Collections.sort(orderPars);
		// 正序显示
		if (orderFlag == 0 | orderFlag == 2) {
			orderFlag = 1;
			int size = orderPars.size();
			for (int i = 0; i < size; i++) {
				formPar[i][0].setText(orderPars.get(i).isIsnull() ? "N" : "Y");
				formPar[i][1].setText(orderPars.get(i).getTip());
				formPar[i][1].setToolTipText(orderPars.get(i).getTip());
				formPar[i][2].setText(orderPars.get(i).getName());
				formPar[i][3].setText(orderPars.get(i).getValue());
				if (orderPars.get(i).isFrozen()) {
					formPar[i][0].setForeground(parFontsFrozenColor);
					formPar[i][1].setForeground(parFontsFrozenColor);
					formPar[i][2].setForeground(parFontsFrozenColor);
					formPar[i][3].setForeground(parFontsFrozenColor);
					menuItem1SubFrozen[i].setText("解冻此参数");
					label[i].setToolTipText("此参数已冻结,冻结后不再发送此参数");
				}
			}
		}
		// 逆序显示
		else {
			orderFlag = 2;
			int size = orderPars.size();
			for (int i = 0; i < size; i++) {
				formPar[i][0].setText(orderPars.get(size - 1 - i).isIsnull() ? "N" : "Y");
				formPar[i][1].setText(orderPars.get(size - 1 - i).getTip());
				formPar[i][1].setToolTipText(orderPars.get(size - 1 - i).getTip());
				formPar[i][2].setText(orderPars.get(size - 1 - i).getName());
				formPar[i][3].setText(orderPars.get(size - 1 - i).getValue());
				if (orderPars.get(size - 1 - i).isFrozen()) {
					formPar[i][0].setForeground(parFontsFrozenColor);
					formPar[i][1].setForeground(parFontsFrozenColor);
					formPar[i][2].setForeground(parFontsFrozenColor);
					formPar[i][3].setForeground(parFontsFrozenColor);
					menuItem1SubFrozen[i].setText("解冻此参数");
					label[i].setToolTipText("此参数已冻结,冻结后不再发送此参数");
				}
			}
		}
	}

	// 给主页面的返回区域添加右键菜单
	private void styledTextAddContextMenu(final StyledText styledText) {
		Menu popupMenu = new Menu(styledText);
		MenuItem cut = new MenuItem(popupMenu, SWT.NONE);
		cut.setText("剪切");
		MenuItem copy = new MenuItem(popupMenu, SWT.NONE);
		copy.setText("复制");
		MenuItem paste = new MenuItem(popupMenu, SWT.NONE);
		paste.setText("粘贴");
		MenuItem allSelect = new MenuItem(popupMenu, SWT.NONE);
		allSelect.setText("全选");
		MenuItem clear = new MenuItem(popupMenu, SWT.NONE);
		clear.setText("清空");
		MenuItem search = new MenuItem(popupMenu, SWT.NONE);
		search.setText("搜索");
		// 响应结果搜索器
		search.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchText(styledText);
			}
		});
		MenuItem compressJson = new MenuItem(popupMenu, SWT.NONE);
		compressJson.setText("压缩JSON");
		MenuItem formatJson = new MenuItem(popupMenu, SWT.NONE);
		formatJson.setText("格式化JSON");
		MenuItem warp = new MenuItem(popupMenu, SWT.NONE);
		warp.setText("自动换行");
		styledText.setMenu(popupMenu);

		// 编码设置
		MenuItem mntmCharsetSelect = new MenuItem(popupMenu, SWT.CASCADE);
		mntmCharsetSelect.setText("字符编码");

		Menu menu = new Menu(mntmCharsetSelect);
		mntmCharsetSelect.setMenu(menu);

		MenuItem menucharSetAuto = new MenuItem(menu, SWT.NONE);
		menucharSetAuto.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				settingResCharSet = "auto";
				updateResultBodyStyledText();
			}
		});
		menucharSetAuto.setText("自动检测");

		MenuItem menucharSetutf8 = new MenuItem(menu, SWT.NONE);
		menucharSetutf8.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				settingResCharSet = "UTF-8";
				updateResultBodyStyledText();
			}
		});
		menucharSetutf8.setText("Unicode (UTF-8)");

		MenuItem menucharSetgbk = new MenuItem(menu, SWT.NONE);
		menucharSetgbk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				settingResCharSet = "GBK";
				updateResultBodyStyledText();
			}
		});
		menucharSetgbk.setText("简体中文 (GBK)");

		MenuItem menuCharsetGb2312 = new MenuItem(menu, SWT.NONE);
		menuCharsetGb2312.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				settingResCharSet = "GB2312";
				updateResultBodyStyledText();
			}
		});
		menuCharsetGb2312.setText("简体中文 (GB2312)");

		MenuItem menucharSetgb18030 = new MenuItem(menu, SWT.NONE);
		menucharSetgb18030.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				settingResCharSet = "GB18030";
				updateResultBodyStyledText();
			}
		});
		menucharSetgb18030.setText("简体中文 (GB18030)");

		MenuItem menucharSetbig5 = new MenuItem(menu, SWT.NONE);
		menucharSetbig5.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				settingResCharSet = "BIG5";
				updateResultBodyStyledText();
			}
		});
		menucharSetbig5.setText("繁体中文 (Big5)");

		MenuItem menucharSetbig5HKSCS = new MenuItem(menu, SWT.NONE);
		menucharSetbig5HKSCS.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				settingResCharSet = "BIG5-HKSCS";
				updateResultBodyStyledText();
			}
		});
		menucharSetbig5HKSCS.setText("繁体中文 (Big5-HKSCS)");

		MenuItem mntmCharsetiso886901 = new MenuItem(menu, SWT.NONE);
		mntmCharsetiso886901.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				settingResCharSet = "ISO-8859-1";
				updateResultBodyStyledText();
			}
		});
		mntmCharsetiso886901.setText("西方 (ISO-8859-1)");

		// 判断初始自动换行状态
		if (styledText.getWordWrap()) {
			warp.setText("关闭自动换行");
		} else {
			warp.setText("打开自动换行");
		}
		styledText.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.stateMask == SWT.CTRL && e.keyCode == 'a') {
					styledText.selectAll();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
		// 剪切菜单的点击事件
		cut.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (styledText.getSelectionCount() == 0) {
					return;
				}
				Clipboard clipboard = new Clipboard(styledText.getDisplay());
				String plainText = styledText.getSelectionText();
				TextTransfer textTransfer = TextTransfer.getInstance();
				clipboard.setContents(new String[] { plainText }, new Transfer[] { textTransfer });
				clipboard.dispose();
				// 将已经剪切走的部分删除,并将插入符移动到剪切位置
				int caretOffset = styledText.getSelection().x;
				styledText.setText(new StringBuffer(styledText.getText())
						.replace(styledText.getSelection().x, styledText.getSelection().y, "").toString());
				styledText.setCaretOffset(caretOffset);
			}
		});

		// 粘贴菜单的点击事件
		paste.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Clipboard clipboard = new Clipboard(styledText.getDisplay());
				TextTransfer textTransfer = TextTransfer.getInstance();
				// 获取剪切板上的文本
				String cliptext = (clipboard.getContents(textTransfer) != null
						? clipboard.getContents(textTransfer).toString()
						: "");
				clipboard.dispose();
				int caretOffset = styledText.getSelection().x;
				styledText.setText(new StringBuffer(styledText.getText())
						.replace(styledText.getSelection().x, styledText.getSelection().y, cliptext).toString());
				styledText.setCaretOffset(caretOffset + cliptext.length());
			}
		});

		// 复制上下文菜单的点击事件
		copy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (styledText.getSelectionCount() == 0) {
					return;
				}
				Clipboard clipboard = new Clipboard(styledText.getDisplay());
				String plainText = styledText.getSelectionText();
				TextTransfer textTransfer = TextTransfer.getInstance();
				clipboard.setContents(new String[] { plainText }, new Transfer[] { textTransfer });
				clipboard.dispose();
			}
		});

		// 全选上下文菜单的点击事件
		allSelect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				styledText.selectAll();
			}
		});

		// 清空上下文菜单的点击事件
		clear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				styledText.setText("");
			}
		});

		// 压缩JSON点击事件
		compressJson.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Pattern p = Pattern.compile("\\s*|\t|\r|\n");
				Matcher m = p.matcher(styledText.getText());
				styledText.setText(m.replaceAll(""));
			}
		});

		// 格式化JSON点击事件
		formatJson.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Pattern p = Pattern.compile("\\s*|\t|\r|\n");
				Matcher m = p.matcher(styledText.getText());
				styledText.setText(PubUtils.jsonFormat(m.replaceAll("")));
			}
		});

		// 更改是否自动换行
		warp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (styledText.getWordWrap()) {
					styledText.setWordWrap(false);
					warp.setText("打开自动换行");
				} else {
					styledText.setWordWrap(true);
					warp.setText("关闭自动换行");
				}
			}
		});
	}

	// 焦点变色
	private void setParTableBackgroundNormal(int b) {
		label[b].setBackground(parBackgroundNormalColor);
		formPar[b][0].setBackground(parBackgroundNormalColor);
		formPar[b][1].setBackground(parBackgroundNormalColor);
		formPar[b][2].setBackground(parBackgroundNormalColor);
		formPar[b][3].setBackground(parBackgroundNormalColor);
	}

	// 还原焦点变色
	private void setParTableBackgroundSelection(int b) {
		label[b].setBackground(parBackgroundSelectedColor);
		formPar[b][0].setBackground(parBackgroundSelectedColor);
		formPar[b][1].setBackground(parBackgroundSelectedColor);
		formPar[b][2].setBackground(parBackgroundSelectedColor);
		formPar[b][3].setBackground(parBackgroundSelectedColor);
	}

	// 开启一个新的窗口
	private void initNewWindow(boolean mainWindowFlag, boolean openByShortcutFlag) {
		MainWindow mainWindow = new MainWindow();
		mainWindow.open(mainWindowFlag, openByShortcutFlag);
	}

	// 拖拽支持
	private void dropTargetSupport(Shell shell) {

		DropTarget dropTarget = new DropTarget(shell, DND.DROP_NONE);
		Transfer[] transfer = new Transfer[] { FileTransfer.getInstance() };
		dropTarget.setTransfer(transfer);
		// 拖拽监听
		dropTarget.addDropListener(new DropTargetListener() {
			@Override
			public void dragEnter(DropTargetEvent event) {
			}

			@Override
			public void dragLeave(DropTargetEvent event) {
			}

			@Override
			public void dragOperationChanged(DropTargetEvent event) {
			}

			@Override
			public void dragOver(DropTargetEvent event) {
			}

			// 获取拖放进来的文件
			@Override
			public void drop(DropTargetEvent event) {
				String[] files = (String[]) event.data;
				for (int i = 0; i < files.length; i++) {
					File file = new File(files[i]);
					logger.debug("检测到拖拽文件:" + file.getPath());
				}
			}

			@Override
			public void dropAccept(DropTargetEvent event) {
			}
		});
	}
}
