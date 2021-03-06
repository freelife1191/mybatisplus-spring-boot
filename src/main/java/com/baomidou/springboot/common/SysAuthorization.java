package com.baomidou.springboot.common;

import com.baomidou.kisso.SSOAuthorization;
import com.baomidou.kisso.Token;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 系统权限授权实现类
 *
 */
@Component
public class SysAuthorization implements SSOAuthorization {

	private static List<String> permissionList = new ArrayList<String>();


	static {
		/**
		 * 正常情况，该部分数据从数据库中加载。
		 */
		permissionList.add("1000");
	}


	/**
	 * 
	 * 开启配置 sso.permission.uri=true 支持、先验证 url 地址，后验证注解。
	 * 
	 */
	public boolean isPermitted(Token token, String permission ) {
		/**
		 * 循环判断权限编码是否合法，token 获取登录用户ID信息、判断相应权限也可作为缓存主键使用。
		 */
		for ( String perm : permissionList ) {
			if ( perm.equals(permission) ) {
				return true;
			}
		}
		return false;
	}

}
