package com.labnex.app.helpers;

import com.labnex.app.models.groups.GroupsItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mmarif
 */
public class GroupsHierarchyBuilder {

	public static List<GroupsItem> buildHierarchyRecursive(List<GroupsItem> flatList) {
		if (flatList == null || flatList.isEmpty()) {
			return new ArrayList<>();
		}

		Map<Long, List<GroupsItem>> childrenByParentId = new HashMap<>();
		List<GroupsItem> rootGroups = new ArrayList<>();

		for (GroupsItem group : flatList) {
			long parentId = group.getParentId();

			if (parentId == 0L) {
				rootGroups.add(group);
			} else {
				childrenByParentId.computeIfAbsent(parentId, k -> new ArrayList<>()).add(group);
			}
		}

		rootGroups.sort((g1, g2) -> g1.getName().compareToIgnoreCase(g2.getName()));

		List<GroupsItem> result = new ArrayList<>();
		for (GroupsItem root : rootGroups) {
			addGroupHierarchy(root, childrenByParentId, result, 0);
		}

		return result;
	}

	private static void addGroupHierarchy(
			GroupsItem group,
			Map<Long, List<GroupsItem>> childrenByParentId,
			List<GroupsItem> result,
			int level) {
		group.setLevel(level);
		result.add(group);

		List<GroupsItem> children = childrenByParentId.get(group.getId());
		if (children != null && !children.isEmpty()) {
			children.sort((g1, g2) -> g1.getName().compareToIgnoreCase(g2.getName()));
			for (GroupsItem child : children) {
				addGroupHierarchy(child, childrenByParentId, result, level + 1);
			}
		}
	}
}
