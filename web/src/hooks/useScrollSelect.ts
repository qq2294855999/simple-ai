import {useCallback, useEffect, useRef, useState} from "react";

/**
 * 分页查询结果泛型。
 */
interface PageResult<T> {
    records: T[];
    total: number;
}

/**
 * useScrollSelect 返回值。
 */
export interface ScrollSelectResult<T> {
    /** 累积的选项列表 */
    options: T[];
    /** 是否正在加载 */
    loading: boolean;
    /** 是否还有更多数据 */
    hasMore: boolean;
    /** 下拉面板滚动事件处理 */
    onPopupScroll: (e: React.UIEvent<HTMLDivElement>) => void;
    /** 重置并重新加载第一页（deps 变化时自动触发，也可手动调用） */
    reset: () => void;
}

/**
 * 下拉滚动分页加载 Hook。
 * 初始加载第一页，用户滚动到底部时自动加载下一页，累积追加到选项列表。
 *
 * @param fetchPage 分页查询函数，接收 page/size，返回 { records, total }
 * @param pageSize 每页条数，默认 20
 * @param deps 依赖数组，变化时自动重置并重新加载第一页
 * @returns {ScrollSelectResult} 选项、加载状态、滚动回调
 *
 * @author qty
 */
export function useScrollSelect<T>(
    fetchPage: (page: number, size: number) => Promise<PageResult<T>>,
    pageSize: number = 20,
    deps: unknown[] = []
): ScrollSelectResult<T> {
    const [options, setOptions] = useState<T[]>([]);
    const [loading, setLoading] = useState(false);
    const [hasMore, setHasMore] = useState(true);

    const pageRef = useRef(1);
    const loadingRef = useRef(false);
    const fetchRef = useRef(fetchPage);
    fetchRef.current = fetchPage;

    // 加载指定页数据
    const loadPage = useCallback(async (page: number) => {
        if (loadingRef.current) return;
        loadingRef.current = true;
        setLoading(true);
        try {
            const result = await fetchRef.current(page, pageSize);
            const records = result.records || [];
            setOptions(prev => page === 1 ? records : [...prev, ...records]);
            setHasMore(records.length >= pageSize);
            pageRef.current = page + 1;
        } finally {
            setLoading(false);
            loadingRef.current = false;
        }
    }, [pageSize]);

    // 下拉面板滚动事件：触底时加载下一页
    const onPopupScroll = useCallback((e: React.UIEvent<HTMLDivElement>) => {
        const target = e.target as HTMLDivElement;
        if (target.scrollHeight - target.scrollTop <= target.clientHeight + 10) {
            if (!loadingRef.current) {
                loadPage(pageRef.current);
            }
        }
    }, [loadPage]);

    // 重置到第一页
    const reset = useCallback(() => {
        pageRef.current = 1;
        setOptions([]);
        setHasMore(true);
        loadingRef.current = false;
        loadPage(1);
    }, [loadPage]);

    // deps 变化时自动重置并重新加载
    useEffect(() => {
        pageRef.current = 1;
        setOptions([]);
        setHasMore(true);
        loadingRef.current = false;
        loadPage(1);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, deps);

    return {options, loading, hasMore, onPopupScroll, reset};
}