// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {

    // 自动关闭警告消息
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });

    // 评论编辑功能
    window.editComment = function(commentId) {
        const content = prompt('请输入新的评论内容：');
        if (content) {
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = `/comment/${commentId}/edit`;
            
            const contentInput = document.createElement('input');
            contentInput.type = 'hidden';
            contentInput.name = 'content';
            contentInput.value = content;
            
            const postIdInput = document.createElement('input');
            postIdInput.type = 'hidden';
            postIdInput.name = 'postId';
            postIdInput.value = document.querySelector('input[name="postId"]').value;
            
            form.appendChild(contentInput);
            form.appendChild(postIdInput);
            document.body.appendChild(form);
            form.submit();
        }
    };

    // 删除确认
    const deleteButtons = document.querySelectorAll('button[onclick*="confirm"]');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (!confirm('确定要删除吗？')) {
                e.preventDefault();
            }
        });
    });

    // 表单验证
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!form.checkValidity()) {
                e.preventDefault();
                e.stopPropagation();
            }
            form.classList.add('was-validated');
        });
    });

    // 搜索框自动聚焦
    const searchInput = document.querySelector('input[name="keyword"]');
    if (searchInput) {
        searchInput.focus();
    }
}); 