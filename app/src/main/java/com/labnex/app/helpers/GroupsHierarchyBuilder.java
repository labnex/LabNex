package com.labnex.app.helpers;

import com.labnex.app.models.groups.GroupsItem;
import java.util.ArrayList;
import java.util.Comparator;
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

		Map<Integer, List<GroupsItem>> childrenByParentId = new HashMap<>();
		List<GroupsItem> rootGroups = new ArrayList<>();

		// Organize data: group children by parent ID
		for (GroupsItem group : flatList) {
			Integer parentId = group.getParentId();

			if (parentId == null || parentId == 0) {
				rootGroups.add(group);
			} else {
				childrenByParentId.computeIfAbsent(parentId, k -> new ArrayList<>()).add(group);
			}
		}

		rootGroups.sort(
				new Comparator<GroupsItem>() {
					@Override
					public int compare(GroupsItem g1, GroupsItem g2) {
						return g1.getName().compareToIgnoreCase(g2.getName());
					}
				});

		// Build result recursively
		List<GroupsItem> result = new ArrayList<>();
		for (GroupsItem root : rootGroups) {
			addGroupHierarchy(root, childrenByParentId, result, 0);
		}

		return result;
	}

	private static void addGroupHierarchy(
			GroupsItem group,
			Map<Integer, List<GroupsItem>> childrenByParentId,
			List<GroupsItem> result,
			int level) {
		group.setLevel(level);
		result.add(group);

		List<GroupsItem> children = childrenByParentId.get(group.getId());
		if (children != null && !children.isEmpty()) {
			children.sort(
					new Comparator<GroupsItem>() {
						@Override
						public int compare(GroupsItem g1, GroupsItem g2) {
							return g1.getName().compareToIgnoreCase(g2.getName());
						}
					});

			for (GroupsItem child : children) {
				addGroupHierarchy(child, childrenByParentId, result, level + 1);
			}
		}
	}
}
