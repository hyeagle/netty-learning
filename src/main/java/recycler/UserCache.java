package recycler;

import io.netty.util.Recycler;

public class UserCache {
    private static final Recycler<User> USER_RECYCLER = new Recycler<User>() {
        @Override
        protected User newObject(Handle<User> handle) {
            User user = new User(handle);
            user.setName("world");
            return user;
        }
    };

    static final class User {
        private String name;
        private Recycler.Handle<User> handle;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public User(Recycler.Handle<User> handle) {
            this.handle = handle;
        }

        public void recycle() {
            handle.recycle(this);
        }
    }

    public static void main(String[] args) {
        User user1 = USER_RECYCLER.get();
        user1.setName("hello");
        // user1.recycle();
        User user2 = USER_RECYCLER.get();  // 对象池没有对象的化，就会 new 一个新的
        System.out.println(user2.getName());
        System.out.println(user1 == user2);
        System.out.println(user2);
        //user2.recycle();
        Thread t1 = new Thread() {
            public void run() {
                User user3 = USER_RECYCLER.get();
                System.out.println(user3);
                user3.recycle();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t1.start();

        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        User user4 = USER_RECYCLER.get();
        System.out.println(user4);
    }
}
